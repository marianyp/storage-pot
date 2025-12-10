package dev.mariany.storagepot.mixin;

import dev.mariany.storagepot.StoragePot;
import dev.mariany.storagepot.client.render.SPTexturedRenderLayers;
import net.minecraft.client.texture.AtlasManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(AtlasManager.class)
public class AtlasManagerMixin {
    @Shadow
    @Final
    @Mutable
    private static List<AtlasManager.Metadata> ATLAS_METADATA;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onInitialize(CallbackInfo ci) {
        List<AtlasManager.Metadata> newAtlas = new ArrayList<>(ATLAS_METADATA);

        newAtlas.add(
                new AtlasManager.Metadata(
                        SPTexturedRenderLayers.STORAGE_POT_ATLAS_TEXTURE,
                        StoragePot.id("storage_pot"),
                        false
                )
        );

        ATLAS_METADATA = newAtlas;
    }
}
