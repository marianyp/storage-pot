package dev.mariany.storagepot.client.render.item.model.special;

import com.mojang.serialization.MapCodec;
import dev.mariany.storagepot.block.entity.StoragePotContents;
import dev.mariany.storagepot.client.render.block.entity.StoragePotBlockEntityRenderer;
import dev.mariany.storagepot.client.render.block.entity.state.StoragePotBlockEntityRenderState;
import dev.mariany.storagepot.item.component.SPComponents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    @Override
    public void render(
            @Nullable StoragePotModelRenderer.StoragePotModelRendererData data,
            ItemDisplayContext displayContext,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            int light,
            int overlay,
            boolean glint,
            int outlineColor
    ) {
        data = Objects.requireNonNullElse(
                data,
                new StoragePotModelRenderer.StoragePotModelRendererData(StoragePotContents.EMPTY, ItemStack.EMPTY)
        );

        StoragePotBlockEntityRenderState storagePotBlockEntityRenderState =
                this.blockEntityRenderer.createRenderState();

        this.blockEntityRenderer.updateRenderState(
                storagePotBlockEntityRenderState,
                data.contents,
                data.waxedItem
        );

        this.blockEntityRenderer.render(
                storagePotBlockEntityRenderState,
                matrices,
                queue,
                light,
                overlay,
                outlineColor
        );
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
        public SpecialModelRenderer<?> bake(BakeContext context) {
            MinecraftClient client = MinecraftClient.getInstance();
            return new StoragePotModelRenderer(
                    new StoragePotBlockEntityRenderer(
                            client.getItemModelManager(),
                            context.entityModelSet(),
                            context.spriteHolder()
                    )
            );
        }
    }

    public record StoragePotModelRendererData(@NotNull StoragePotContents contents, @NotNull ItemStack waxedItem) {
    }
}
