package dev.mariany.storagepot.mixin;

import dev.mariany.storagepot.StoragePot;
import dev.mariany.storagepot.client.render.SPTexturedRenderLayers;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(BakedModelManager.class)
public class BakedModelManagerMixin {
    @Shadow
    @Final
    @Mutable
    private static Map<Identifier, Identifier> LAYERS_TO_LOADERS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onClinit(CallbackInfo ci) {
        Map<Identifier, Identifier> newMap = new HashMap<>(LAYERS_TO_LOADERS);
        newMap.put(SPTexturedRenderLayers.STORAGE_POT_ATLAS_TEXTURE, StoragePot.id("storage_pot"));
        LAYERS_TO_LOADERS = newMap;
    }
}
