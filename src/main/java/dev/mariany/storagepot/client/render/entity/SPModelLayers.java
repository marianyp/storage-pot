package dev.mariany.storagepot.client.render.entity;

import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class SPModelLayers {
    public static final EntityModelLayer STORAGE_POT_BASE = create("storage_pot_base");
    public static final EntityModelLayer STORAGE_POT_SIDES = create("storage_pot_sides");

    private static EntityModelLayer create(String id) {
        return create(id, "main");
    }

    private static EntityModelLayer create(String id, String layer) {
        return new EntityModelLayer(Identifier.ofVanilla(id), layer);
    }
}
