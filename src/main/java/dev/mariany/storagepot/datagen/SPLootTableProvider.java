package dev.mariany.storagepot.datagen;

import dev.mariany.storagepot.block.SPBlocks;
import dev.mariany.storagepot.item.component.SPComponents;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.CopyComponentsLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class SPLootTableProvider extends FabricBlockLootTableProvider {
    public SPLootTableProvider(
            FabricDataOutput dataOutput,
            CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup
    ) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        addDrop(SPBlocks.STORAGE_POT, storagePotDrops(SPBlocks.STORAGE_POT));
    }

    private LootTable.Builder storagePotDrops(Block pot) {
        return LootTable.builder().pool(this.addSurvivesExplosionCondition(pot,
                LootPool.builder().rolls(ConstantLootNumberProvider.create(1)).with(ItemEntry.builder(pot)
                        .apply(CopyComponentsLootFunction.builder(CopyComponentsLootFunction.Source.BLOCK_ENTITY)
                                                         .include(SPComponents.CONTENTS).include(SPComponents.WAXED)))));
    }
}
