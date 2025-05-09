package dev.mariany.storagepot.block.entity;

import dev.mariany.storagepot.StoragePot;
import dev.mariany.storagepot.block.SPBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class SPBlockEntities {
    public static final BlockEntityType<StoragePotBlockEntity> STORAGE_POT = register("storage_pot",
            FabricBlockEntityTypeBuilder.create(StoragePotBlockEntity::new, SPBlocks.STORAGE_POT).build());

    public static <T extends BlockEntityType<?>> T register(String path, T blockEntityType) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, StoragePot.id(path), blockEntityType);
    }

    public static void bootstrap() {
        StoragePot.LOGGER.info("Registering block entities for " + StoragePot.MOD_ID);
    }
}
