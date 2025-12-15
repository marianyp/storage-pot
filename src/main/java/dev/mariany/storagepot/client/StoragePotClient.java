package dev.mariany.storagepot.client;

import com.mojang.serialization.MapCodec;
import dev.mariany.storagepot.StoragePot;
import dev.mariany.storagepot.block.SPBlocks;
import dev.mariany.storagepot.block.entity.SPBlockEntities;
import dev.mariany.storagepot.client.render.block.entity.StoragePotBlockEntityRenderer;
import dev.mariany.storagepot.client.render.entity.SPModelLayers;
import dev.mariany.storagepot.client.render.item.model.special.StoragePotModelRenderer;
import dev.mariany.storagepot.event.item.ItemTooltipHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialBlockRendererRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.render.item.model.special.SpecialModelTypes;
import net.minecraft.util.Identifier;

public class StoragePotClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        registerSpecialBlockRenderers();
        registerEntityModelLayers();
        registerBlockEntityRenderers();
        registerTooltipHandlers();
    }

    private void registerTooltipHandlers() {
        ItemTooltipCallback.EVENT.register(ItemTooltipHandler::getTooltip);
    }

    private <T extends SpecialModelRenderer.Unbaked> void registerSpecialBlockRenderer(
            Identifier id, Block block,
            T unbakedRenderer,
            MapCodec<T> codec
    ) {
        SpecialBlockRendererRegistry.register(block, unbakedRenderer);
        SpecialModelTypes.ID_MAPPER.put(id, codec);
    }

    private void registerSpecialBlockRenderers() {
        StoragePot.LOGGER.info("Registering special block renderers for mod " + StoragePot.MOD_ID);

        registerSpecialBlockRenderer(
                StoragePot.id("storage_pot"),
                SPBlocks.STORAGE_POT,
                new StoragePotModelRenderer.Unbaked(),
                StoragePotModelRenderer.Unbaked.CODEC
        );
    }

    private void registerEntityModelLayers() {
        StoragePot.LOGGER.info("Registering entity model layers for mod " + StoragePot.MOD_ID);

        EntityModelLayerRegistry.registerModelLayer(
                SPModelLayers.STORAGE_POT_BASE,
                StoragePotBlockEntityRenderer::getTopBottomNeckTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(
                SPModelLayers.STORAGE_POT_SIDES,
                StoragePotBlockEntityRenderer::getSidesTexturedModelData);
    }

    private void registerBlockEntityRenderers() {
        StoragePot.LOGGER.info("Registering block entity renderers for mod " + StoragePot.MOD_ID);

        BlockEntityRendererFactories.register(SPBlockEntities.STORAGE_POT, StoragePotBlockEntityRenderer::new);
    }
}
