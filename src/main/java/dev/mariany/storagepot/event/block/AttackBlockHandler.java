package dev.mariany.storagepot.event.block;

import dev.mariany.storagepot.block.StoragePotBlock;
import dev.mariany.storagepot.block.entity.StoragePotBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class AttackBlockHandler {
    public static ActionResult onAttack(
            PlayerEntity player,
            World world,
            Hand hand,
            BlockPos pos,
            Direction direction
    ) {
        return handleStoragePotExtraction(player, pos, direction);
    }

    public static ActionResult handleStoragePotExtraction(PlayerEntity player, BlockPos pos, Direction direction) {
        World world = player.getEntityWorld();

        if (!StoragePotBlock.isExtractionSide(player, pos, direction)) {
            return ActionResult.PASS;
        }

        if (world.getBlockEntity(pos) instanceof StoragePotBlockEntity storagePotBlockEntity) {
            if (storagePotBlockEntity.isEmpty()) {
                if (player.isSneaking()) {
                    return ActionResult.PASS;
                }
            } else if (!storagePotBlockEntity.isOnCooldown() && storagePotBlockEntity.extract(player)) {
                storagePotBlockEntity.onChange();
                storagePotBlockEntity.triggerCooldown();
                world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            }
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}
