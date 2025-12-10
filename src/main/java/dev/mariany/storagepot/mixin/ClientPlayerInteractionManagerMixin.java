package dev.mariany.storagepot.mixin;

import dev.mariany.storagepot.block.StoragePotBlock;
import dev.mariany.storagepot.block.entity.StoragePotBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "isCurrentlyBreaking", at = @At(value = "RETURN"), cancellable = true)
    private void injectIsCurrentlyBreaking(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = this.client.player;

        if (player != null) {
            if (player.getEntityWorld().getBlockEntity(pos) instanceof StoragePotBlockEntity storagePotBlockEntity) {
                if (StoragePotBlock.isExtractionSide(player)) {
                    if (!storagePotBlockEntity.isEmpty() || !player.isSneaking()) {
                        cir.setReturnValue(false);
                    }
                }
            }
        }
    }
}
