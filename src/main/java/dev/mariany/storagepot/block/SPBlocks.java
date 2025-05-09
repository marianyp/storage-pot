package dev.mariany.storagepot.block;

import dev.mariany.storagepot.StoragePot;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class SPBlocks {
    public static final Block STORAGE_POT = register("storage_pot", StoragePotBlock::new,
            AbstractBlock.Settings.create().mapColor(MapColor.TERRACOTTA_RED).strength(0.6F)
                    .pistonBehavior(PistonBehavior.DESTROY).nonOpaque());

    private static Block register(String name, Function<AbstractBlock.Settings, Block> factory,
                                  AbstractBlock.Settings settings) {
        final Identifier identifier = StoragePot.id(name);
        final RegistryKey<Block> registryKey = RegistryKey.of(RegistryKeys.BLOCK, identifier);

        final Block block = Blocks.register(registryKey, factory, settings);
        Items.register(block);

        return block;
    }

    public static void bootstrap() {
        StoragePot.LOGGER.info("Registering blocks for " + StoragePot.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
                .register(entries -> entries.addAfter(Items.DECORATED_POT, STORAGE_POT));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE)
                .register(entries -> entries.addAfter(Items.DECORATED_POT, STORAGE_POT));
    }
}
