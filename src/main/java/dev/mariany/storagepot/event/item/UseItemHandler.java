package dev.mariany.storagepot.event.item;

import dev.mariany.storagepot.block.StoragePotBlock;
import dev.mariany.storagepot.block.entity.StoragePotBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UseItemHandler {
    public static ActionResult onInteract(PlayerEntity player, World world, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        BlockHitResult blockHitResult = StoragePotBlock.getBlockHitResult(player);

        if (blockHitResult != null) {
            BlockPos blockPos = blockHitResult.getBlockPos();
            BlockState blockState = world.getBlockState(blockPos);

            if (StoragePotBlockEntity.shouldCancelInteraction(world, blockPos, stack)) {
                blockState.onUseWithItem(stack, world, player, hand, blockHitResult);
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }
}
