package dev.mariany.storagepot.datagen;

import dev.mariany.storagepot.StoragePot;
import dev.mariany.storagepot.client.render.SPTexturedRenderLayers;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.texture.atlas.AtlasSource;
import net.minecraft.client.texture.atlas.AtlasSourceManager;
import net.minecraft.client.texture.atlas.DirectoryAtlasSource;
import net.minecraft.client.util.SpriteMapper;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SPAtlasDefinitionProvider implements DataProvider {
    private final DataOutput.PathResolver pathResolver;

    public SPAtlasDefinitionProvider(FabricDataOutput output) {
        this.pathResolver = output.getResolver(DataOutput.OutputType.RESOURCE_PACK, "atlases");
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return CompletableFuture.allOf(this.runForAtlas(writer, StoragePot.id("storage_pot"),
                                                        createAtlasSources(SPTexturedRenderLayers.STORAGE_POT_SPRITE_MAPPER)));
    }


    private CompletableFuture<?> runForAtlas(DataWriter writer, Identifier atlasId, List<AtlasSource> atlasSources) {
        return DataProvider.writeCodecToPath(writer, AtlasSourceManager.LIST_CODEC, atlasSources,
                this.pathResolver.resolveJson(atlasId));
    }

    private static List<AtlasSource> createAtlasSources(SpriteMapper spriteMapper) {
        return List.of(createDirectoryAtlasSource(spriteMapper));
    }

    private static AtlasSource createDirectoryAtlasSource(SpriteMapper spriteMapper) {
        return new DirectoryAtlasSource(spriteMapper.prefix(), spriteMapper.prefix() + "/");
    }

    @Override
    public String getName() {
        return "Storage Pots Atlas Definitions";
    }
}
