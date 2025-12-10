package dev.mariany.storagepot.block;

import com.mojang.serialization.MapCodec;
import dev.mariany.storagepot.block.entity.StoragePotBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class StoragePotBlock extends BlockWithEntity {
    public static final MapCodec<StoragePotBlock> CODEC = createCodec(StoragePotBlock::new);
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Block.createColumnShape(16.0, 0.0, 16.0);

    public StoragePotBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    public static Optional<BlockHitResult> getExtractionSide(PlayerEntity player) {
        BlockHitResult blockHitResult = getBlockHitResult(player);

        if (blockHitResult != null) {
            if (isExtractionSide(player, blockHitResult)) {
                return Optional.of(blockHitResult);
            }
        }

        return Optional.empty();
    }

    public static boolean isExtractionSide(PlayerEntity player) {
        return getExtractionSide(player).isPresent();
    }

    public static boolean isExtractionSide(PlayerEntity player, BlockHitResult blockHitResult) {
        return isExtractionSide(player, blockHitResult.getBlockPos(), blockHitResult.getSide());
    }

    public static boolean isExtractionSide(PlayerEntity player, BlockPos pos, Direction side) {
        BlockState state = player.getEntityWorld().getBlockState(pos);

        if (state.getBlock() instanceof StoragePotBlock) {
            Optional<Direction> optionalFacing = state.getOrEmpty(StoragePotBlock.FACING);

            if (optionalFacing.isPresent()) {
                return side.equals(optionalFacing.get().getOpposite());
            }
        }

        return false;
    }

    public static BlockHitResult getBlockHitResult(PlayerEntity player) {
        if (player.raycast(player.getBlockInteractionRange(), 1, false) instanceof BlockHitResult blockHitResult) {
            return blockHitResult;
        }

        return null;
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing());
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
                                         PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof StoragePotBlockEntity storagePotBlockEntity) {
            boolean waxing = stack.getItem().equals(Items.HONEYCOMB);
            boolean scraping = stack.getItem() instanceof AxeItem;
            boolean waxed = storagePotBlockEntity.isWaxed();

            if (waxing || scraping) {
                boolean extractionSide = isExtractionSide(player, hit);
                boolean canInsert = storagePotBlockEntity.canInsert(stack);
                boolean isWaxedItem = ItemStack.areItemsAndComponentsEqual(storagePotBlockEntity.getWaxedItem(), stack);

                if (waxing && !waxed && (!extractionSide || !canInsert)) {
                    if (storagePotBlockEntity.wax()) {
                        stack.decrementUnlessCreative(1, player);
                        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(player, state));
                        world.syncWorldEvent(player, WorldEvents.BLOCK_WAXED, pos, 0);
                        return ActionResult.SUCCESS;
                    }
                }

                if (scraping && waxed && (!extractionSide || !isWaxedItem)) {
                    if (storagePotBlockEntity.unwax()) {
                        stack.damage(1, player);
                        world.playSound(player, pos, SoundEvents.ITEM_AXE_WAX_OFF, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        world.syncWorldEvent(player, WorldEvents.WAX_REMOVED, pos, 0);
                        return ActionResult.SUCCESS;
                    }
                }
            }

            if (world.isClient()) {
                return ActionResult.SUCCESS;
            }

            if (storagePotBlockEntity.add(player, stack, player.isSneaking())) {
                storagePotBlockEntity.onChange();
                world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                return ActionResult.SUCCESS;
            }

            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }

        return ActionResult.PASS;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof StoragePotBlockEntity storagePotBlockEntity) {
            world.playSound(null, pos, SoundEvents.BLOCK_DECORATED_POT_INSERT_FAIL, SoundCategory.BLOCKS, 1.0F, 1.0F);
            storagePotBlockEntity.wobble(DecoratedPotBlockEntity.WobbleType.NEGATIVE);
            world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (world.getBlockEntity(pos) instanceof StoragePotBlockEntity storagePotBlockEntity) {
            if (!world.isClient() && player.shouldSkipBlockDrops() && !storagePotBlockEntity.isEmpty()) {
                ItemStack itemStack = new ItemStack(SPBlocks.STORAGE_POT);
                itemStack.applyComponentsFrom(storagePotBlockEntity.createComponentMap());
                ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        itemStack);
                itemEntity.setToDefaultPickupDelay();
                world.spawnEntity(itemEntity);
            }
        }

        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new StoragePotBlockEntity(pos, state);
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        ItemScatterer.onStateReplaced(state, world, pos);
    }

    @Override
    protected BlockSoundGroup getSoundGroup(BlockState state) {
        return BlockSoundGroup.DECORATED_POT;
    }

    @Override
    protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        BlockPos blockPos = hit.getBlockPos();
        if (world instanceof ServerWorld serverWorld && projectile.canModifyAt(serverWorld,
                blockPos) && projectile.canBreakBlocks(serverWorld)) {
            world.breakBlock(blockPos, true, projectile);
        }
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }
}
