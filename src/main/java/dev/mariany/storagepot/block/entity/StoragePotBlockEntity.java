package dev.mariany.storagepot.block.entity;

import dev.mariany.storagepot.item.component.SPComponents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class StoragePotBlockEntity extends BlockEntity implements Inventory {
    private static final String CONTENTS_NBT = "Contents";
    private static final String WAXED_NBT = "Waxed";
    private static final int EXTRACT_COOLDOWN_TICKS = 3;
    private static final int VANILLA_CHEST_SIZE = 27;

    private ItemStack waxed = ItemStack.EMPTY;
    private StoragePotContents contents = StoragePotContents.EMPTY;
    private long lastExtractTime;
    public long lastWobbleTime;
    @Nullable
    public DecoratedPotBlockEntity.WobbleType lastWobbleType;

    public StoragePotBlockEntity(BlockPos pos, BlockState state) {
        this(SPBlockEntities.STORAGE_POT, pos, state);
    }

    public StoragePotBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean canInsert(ItemStack stack) {
        if (!this.waxed.isEmpty() && !ItemStack.areItemsAndComponentsEqual(stack, this.waxed)) {
            return false;
        }

        if (this.contents.isEmpty()) {
            return true;
        }

        if (ItemStack.areItemsAndComponentsEqual(stack, this.contents.getBaseStack())) {
            return !this.isFull();
        }

        return false;
    }

    public static boolean shouldCancelInteraction(World world, BlockPos blockPos, ItemStack stack) {
        if (world.getBlockEntity(blockPos) instanceof StoragePotBlockEntity storagePotBlockEntity) {
            return storagePotBlockEntity.canInsert(stack);
        }

        return false;
    }

    public StoragePotContents getContents() {
        return this.contents;
    }

    public ItemStack getWaxedItem() {
        return this.waxed;
    }

    public boolean isWaxed() {
        return !this.waxed.isEmpty();
    }

    public boolean wax() {
        if (this.contents.isEmpty() || this.isWaxed()) {
            return false;
        }

        this.waxed = this.contents.getBaseStack().copyWithCount(1);
        this.updateListeners();

        return true;
    }

    public boolean unwax() {
        if (this.isWaxed()) {
            this.waxed = ItemStack.EMPTY;
            this.updateListeners();
            return true;
        }

        return false;
    }

    public boolean add(@Nullable PlayerEntity player, ItemStack stack, boolean all) {
        if (this.world == null || this.world.isClient()) {
            return false;
        }

        if (this.contents.count() >= this.getCapacity()) {
            return false;
        }

        if (!stack.isEmpty() && canInsert(stack)) {
            int capacity = this.getCapacity();
            int contentsAmount = this.contents.count();
            int stackCount = stack.getCount();
            int space = Math.max(0, capacity - contentsAmount);
            int amount = all ? Math.min(space, stackCount) : 1;

            this.contents = new StoragePotContents(
                    stack.getRegistryEntry(), amount + contentsAmount,
                    stack.getComponentChanges()
            );
            stack.splitUnlessCreative(amount, player);

            this.updateListeners();

            return amount > 0;
        }

        return false;
    }

    public boolean extract(PlayerEntity player) {
        if (player.getWorld().isClient()) {
            return false;
        }

        ItemStack baseStack = this.contents.getBaseStack();
        int contentsAmount = this.contents.count();
        int maxCount = baseStack.getMaxCount();
        int amount = Math.min(contentsAmount, player.isSneaking() ? maxCount : 1);
        this.contents = this.contents.withCount(oldCount -> oldCount - amount);
        ItemStack extracted = baseStack.copyWithCount(amount);

        this.updateListeners();

        if (!extracted.isEmpty()) {
            giveItemStack(player, extracted);
            return true;
        }

        return false;
    }

    private static void giveItemStack(PlayerEntity player, ItemStack stack) {
        int remainingCount = stack.getCount();
        int maxStackSize = stack.getMaxCount();

        while (remainingCount > 0) {
            int currentSplitCount = Math.min(maxStackSize, remainingCount);
            remainingCount -= currentSplitCount;
            ItemStack splitStack = stack.copyWithCount(currentSplitCount);
            boolean successfullyInserted = player.getInventory().insertStack(splitStack);

            if (successfullyInserted && splitStack.isEmpty()) {
                ItemEntity itemEntity = player.dropItem(stack, false);

                if (itemEntity != null) {
                    itemEntity.setDespawnImmediately();
                }

                player.getWorld()
                      .playSound(
                              null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ITEM_PICKUP,
                              SoundCategory.PLAYERS, 0.2F, ((player.getRandom().nextFloat() - player.getRandom()
                                                                                                    .nextFloat()) *
                                      0.7F + 1.0F) * 2.0F
                      );
            } else {
                ItemEntity itemEntity = player.dropItem(splitStack, false);

                if (itemEntity != null) {
                    itemEntity.resetPickupDelay();
                    itemEntity.setOwner(player.getUuid());
                    itemEntity.setVelocity(Vec3d.ZERO);
                }
            }
        }
    }

    public int getFillPercentage() {
        return MathHelper.clamp(this.contents.count() / this.getCapacity(), 0, 1);
    }

    public int getCapacity() {
        return VANILLA_CHEST_SIZE * 2 * 64;
    }

    public void wobble(DecoratedPotBlockEntity.WobbleType wobbleType) {
        if (this.world != null && !this.world.isClient()) {
            this.world.addSyncedBlockEvent(this.getPos(), this.getCachedState().getBlock(), 1, wobbleType.ordinal());
        }
    }

    public void onChange() {
        this.wobble(DecoratedPotBlockEntity.WobbleType.POSITIVE);

        if (this.world != null) {
            world.playSound(
                    null, pos, SoundEvents.BLOCK_DECORATED_POT_INSERT, SoundCategory.BLOCKS, 1.0F,
                    0.7F + 0.5F * this.getFillPercentage()
            );

            if (this.world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(
                        ParticleTypes.DUST_PLUME, pos.getX() + 0.5, pos.getY() + 1.2,
                        pos.getZ() + 0.5, 7, 0.0, 0.0, 0.0, 0.0
                );
            }
        }
    }

    public void triggerCooldown() {
        if (this.world != null) {
            this.lastExtractTime = this.world.getTime();
        }
    }

    public boolean isOnCooldown() {
        if (this.world == null) {
            return false;
        }

        return (this.world.getTime() - this.lastExtractTime) < EXTRACT_COOLDOWN_TICKS;
    }

    public Direction getHorizontalFacing() {
        return this.getCachedState().get(Properties.HORIZONTAL_FACING);
    }

    @Override
    public boolean onSyncedBlockEvent(int type, int data) {
        if (this.world != null && type == 1 && data >= 0 && data < DecoratedPotBlockEntity.WobbleType.values().length) {
            this.lastWobbleTime = this.world.getTime();
            this.lastWobbleType = DecoratedPotBlockEntity.WobbleType.values()[data];
            return true;
        } else {
            return super.onSyncedBlockEvent(type, data);
        }
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);

        if (!this.contents.isEmpty()) {
            view.put(CONTENTS_NBT, StoragePotContents.CODEC, this.contents);
        }

        if (!this.waxed.isEmpty()) {
            view.put(WAXED_NBT, ItemStack.CODEC, this.waxed);
        }
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);

        Optional<StoragePotContents> contents = view.read(CONTENTS_NBT, StoragePotContents.CODEC);
        Optional<ItemStack> waxed = view.read(WAXED_NBT, ItemStack.CODEC);

        contents.ifPresentOrElse(
                storagePotContents -> this.contents = storagePotContents,
                () -> this.contents = StoragePotContents.EMPTY
        );

        waxed.ifPresentOrElse(
                waxedItem -> this.waxed = waxedItem,
                () -> this.waxed = ItemStack.EMPTY
        );
    }

    @Override
    protected void addComponents(ComponentMap.Builder builder) {
        super.addComponents(builder);

        if (!this.contents.isEmpty()) {
            builder.add(SPComponents.CONTENTS, this.contents);
        }

        if (!this.waxed.isEmpty()) {
            builder.add(SPComponents.WAXED, this.waxed);
        }
    }

    @Override
    protected void readComponents(ComponentsAccess components) {
        super.readComponents(components);
        this.contents = components.getOrDefault(SPComponents.CONTENTS, StoragePotContents.EMPTY);
        this.waxed = components.getOrDefault(SPComponents.WAXED, ItemStack.EMPTY);
    }

    private void updateListeners() {
        World world = this.world;

        if (world != null) {
            this.markDirty();
            world.updateListeners(this.getPos(), this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
        }
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return this.createComponentlessNbt(registries);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot == 0) {
            return this.contents.toStack(1);
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (ItemStack.areItemsAndComponentsEqual(stack, this.contents.getBaseStack())) {
            if (slot == 0) {
                List<ItemStack> items = this.contents.toStacks();

                if (items.isEmpty()) {
                    this.contents = StoragePotContents.from(List.of(stack));
                } else {
                    int lastStackCount = items.getLast().getCount();
                    int difference = lastStackCount - stack.getCount();
                    this.contents = this.contents.withCount(oldCount -> oldCount - difference);
                }

                this.updateListeners();
            }
        }
    }

    @Override
    public ItemStack removeStack(int slot) {
        return remove(slot, -1);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return remove(slot, amount);
    }

    private ItemStack remove(int slot, int amount) {
        if (this.getCapacity() > slot) {
            ItemStack baseStack = this.contents.getBaseStack();

            int maxCount = baseStack.getMaxCount();
            int count = baseStack.getCount();
            int clampedStack = Math.min(maxCount, count);
            int removeAmount = amount < 0 ? clampedStack : Math.min(amount, clampedStack);

            this.contents = this.contents.withCount(oldCount -> oldCount - removeAmount);
            this.updateListeners();

            return baseStack.copyWithCount(removeAmount);
        }

        return ItemStack.EMPTY;
    }


    @Override
    public void clear() {
        this.contents = StoragePotContents.EMPTY;
        this.updateListeners();
    }

    @Override
    public boolean isEmpty() {
        return this.contents.isEmpty();
    }

    public boolean isFull() {
        return this.contents.count() >= this.getCapacity();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        // Overwrite to prevent item scattering
    }
}
