package dev.mariany.storagepot;

import dev.mariany.storagepot.datagen.*;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class StoragePotDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(SPBlockTagProvider::new);
        pack.addProvider(SPRecipeProvider::new);
        pack.addProvider(SPLootTableProvider::new);
        pack.addProvider(SPModelProvider::new);
        pack.addProvider(SPAtlasDefinitionProvider::new);
    }
}
