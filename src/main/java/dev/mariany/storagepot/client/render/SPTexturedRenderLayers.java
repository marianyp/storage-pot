package dev.mariany.storagepot.client.render;

import dev.mariany.storagepot.StoragePot;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.SpriteMapper;
import net.minecraft.util.Identifier;

public class SPTexturedRenderLayers {
    public static final Identifier STORAGE_POT_ATLAS_TEXTURE = StoragePot.id("textures/atlas/storage_pot.png");
    public static final SpriteMapper STORAGE_POT_SPRITE_MAPPER = new SpriteMapper(STORAGE_POT_ATLAS_TEXTURE,
            "entity/storage_pot");
    public static final SpriteIdentifier STORAGE_POT_BASE = STORAGE_POT_SPRITE_MAPPER.map(
            StoragePot.id("storage_pot_base"));
    public static final SpriteIdentifier STORAGE_POT_SIDE = STORAGE_POT_SPRITE_MAPPER.map(
            StoragePot.id("storage_pot_side"));
    public static final SpriteIdentifier STORAGE_POT_FRONT = STORAGE_POT_SPRITE_MAPPER.map(
            StoragePot.id("storage_pot_front"));
}
