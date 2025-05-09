package dev.mariany.storagepot.client.render.item.model.special;

import com.mojang.serialization.MapCodec;
import dev.mariany.storagepot.block.entity.StoragePotContents;
import dev.mariany.storagepot.client.render.block.entity.StoragePotBlockEntityRenderer;
import dev.mariany.storagepot.item.component.SPComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.Set;

public class StoragePotModelRenderer
        implements SpecialModelRenderer<StoragePotModelRenderer.StoragePotModelRendererData> {
    private final StoragePotBlockEntityRenderer blockEntityRenderer;

    public StoragePotModelRenderer(StoragePotBlockEntityRenderer blockEntityRenderer) {
        this.blockEntityRenderer = blockEntityRenderer;
    }

    public StoragePotModelRendererData getData(ItemStack itemStack) {
        return new StoragePotModelRendererData(
                itemStack.getOrDefault(SPComponents.CONTENTS, StoragePotContents.EMPTY),
                itemStack.getOrDefault(SPComponents.WAXED, ItemStack.EMPTY)
        );
    }

    public void render(
            StoragePotModelRenderer.StoragePotModelRendererData data,
            ItemDisplayContext itemDisplayContext,
            MatrixStack matrixStack,
            VertexConsumerProvider vertexConsumerProvider,
            int light,
            int overlay,
            boolean bl
    ) {
        data = Objects.requireNonNullElse(
                data,
                new StoragePotModelRenderer.StoragePotModelRendererData(StoragePotContents.EMPTY, ItemStack.EMPTY)
        );
        this.blockEntityRenderer.render(matrixStack, vertexConsumerProvider, light, overlay, data.contents, data.waxedItem);
    }

    @Override
    public void collectVertices(Set<Vector3f> vertices) {
        this.blockEntityRenderer.collectVertices(vertices);
    }

    @Environment(EnvType.CLIENT)
    public record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<StoragePotModelRenderer.Unbaked> CODEC = MapCodec.unit(
                new StoragePotModelRenderer.Unbaked());

        @Override
        public MapCodec<StoragePotModelRenderer.Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(LoadedEntityModels entityModels) {
            MinecraftClient client = MinecraftClient.getInstance();
            return new StoragePotModelRenderer(
                    new StoragePotBlockEntityRenderer(client.getItemRenderer(), null, entityModels)
            );
        }
    }

    public record StoragePotModelRendererData(@NotNull StoragePotContents contents, @NotNull ItemStack waxedItem) {
    }
}
