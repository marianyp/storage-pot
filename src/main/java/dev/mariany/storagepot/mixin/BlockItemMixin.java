package dev.mariany.storagepot.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mariany.storagepot.block.StoragePotBlock;
import dev.mariany.storagepot.block.entity.StoragePotBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @WrapOperation(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BlockItem;place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;"))
    public ActionResult wrapPlace(BlockItem instance, ItemPlacementContext context, Operation<ActionResult> original) {
        ItemStack stack = context.getStack();
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();

        Optional<BlockHitResult> optionalBlockHitResult = StoragePotBlock.getExtractionSide(player);

        if (optionalBlockHitResult.isPresent()) {
            BlockHitResult blockHitResult = optionalBlockHitResult.get();
            BlockPos blockPos = blockHitResult.getBlockPos();

            if (StoragePotBlockEntity.shouldCancelInteraction(world, blockPos, stack)) {
                BlockState blockState = world.getBlockState(blockPos);
                blockState.onUseWithItem(stack, world, player, context.getHand(), blockHitResult);
                return ActionResult.FAIL;
            }
        }

        return original.call(instance, context);
    }
}
