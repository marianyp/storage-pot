package dev.mariany.storagepot.datagen;

import dev.mariany.storagepot.block.SPBlocks;
import dev.mariany.storagepot.client.render.item.model.special.StoragePotModelRenderer;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.Blocks;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;

public class SPModelProvider extends FabricModelProvider {
    public SPModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        blockStateModelGenerator.registerBuiltinWithParticle(SPBlocks.STORAGE_POT, Blocks.TERRACOTTA);
        blockStateModelGenerator.registerSpecialItemModel(
                SPBlocks.STORAGE_POT,
                new StoragePotModelRenderer.Unbaked());
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
    }
}
