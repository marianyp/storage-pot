package dev.mariany.storagepot.client.render.block.entity;

import dev.mariany.storagepot.block.entity.StoragePotBlockEntity;
import dev.mariany.storagepot.block.entity.StoragePotContents;
import dev.mariany.storagepot.client.render.SPTexturedRenderLayers;
import dev.mariany.storagepot.client.render.entity.SPModelLayers;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class StoragePotBlockEntityRenderer implements BlockEntityRenderer<StoragePotBlockEntity> {
    private static final String FRONT = "front";
    private static final String BACK = "back";
    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final String TOP = "top";

    private final ItemRenderer itemRenderer;
    @Nullable
    private final TextRenderer textRenderer;

    private final ModelPart neck;
    private final ModelPart front;
    private final ModelPart back;
    private final ModelPart left;
    private final ModelPart right;
    private final ModelPart top;
    private final ModelPart bottom;

    public StoragePotBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this(context.getItemRenderer(), context.getTextRenderer(), context.getLoadedEntityModels());
    }

    public StoragePotBlockEntityRenderer(
            ItemRenderer itemRenderer, @Nullable TextRenderer textRenderer,
            LoadedEntityModels models
    ) {
        ModelPart basePart = models.getModelPart(SPModelLayers.STORAGE_POT_BASE);
        this.neck = basePart.getChild(EntityModelPartNames.NECK);
        this.top = basePart.getChild(TOP);
        this.bottom = basePart.getChild(EntityModelPartNames.BOTTOM);

        ModelPart sidePart = models.getModelPart(SPModelLayers.STORAGE_POT_SIDES);
        this.front = sidePart.getChild(FRONT);
        this.back = sidePart.getChild(BACK);
        this.left = sidePart.getChild(LEFT);
        this.right = sidePart.getChild(RIGHT);

        this.itemRenderer = itemRenderer;
        this.textRenderer = textRenderer;
    }

    public static TexturedModelData getTopBottomNeckTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        Dilation dilation = new Dilation(0.2F);
        Dilation dilation2 = new Dilation(-0.1F);
        modelPartData.addChild(
                EntityModelPartNames.NECK,
                ModelPartBuilder.create().uv(0, 0).cuboid(4.0F, 17.0F, 4.0F, 8.0F, 3.0F, 8.0F, dilation2).uv(0, 5)
                                .cuboid(5.0F, 20.0F, 5.0F, 6.0F, 1.0F, 6.0F, dilation),
                ModelTransform.of(0.0F, 37.0F, 16.0F, (float) Math.PI, 0.0F, 0.0F)
        );
        ModelPartBuilder modelPartBuilder = ModelPartBuilder.create().uv(-16, 13)
                                                            .cuboid(0.0F, 0.0F, 0.0F, 16.0F, 0.0F, 16.0F);
        modelPartData.addChild("top", modelPartBuilder, ModelTransform.of(0.0F, 16.0F, 0.0F, 0.0F, 0.0F, 0.0F));
        modelPartData.addChild(
                EntityModelPartNames.BOTTOM, modelPartBuilder,
                ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
        );
        return TexturedModelData.of(modelData, 32, 32);
    }

    public static TexturedModelData getSidesTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartBuilder modelPartBuilder = ModelPartBuilder.create().uv(0, 0)
                                                            .cuboid(
                                                                    0.0F,
                                                                    0.0F,
                                                                    0.0F,
                                                                    16.0F,
                                                                    16.0F,
                                                                    0.0F,
                                                                    EnumSet.of(Direction.NORTH)
                                                            );
        modelPartData.addChild("back", modelPartBuilder, ModelTransform.of(16, 16.0F, 0F, 0.0F, 0.0F, (float) Math.PI));
        modelPartData.addChild(
                "left", modelPartBuilder,
                ModelTransform.of(0F, 16.0F, 0F, 0.0F, (float) (-Math.PI / 2), (float) Math.PI)
        );
        modelPartData.addChild(
                "right", modelPartBuilder,
                ModelTransform.of(16, 16.0F, 16, 0.0F, (float) (Math.PI / 2), (float) Math.PI)
        );
        modelPartData.addChild(
                "front", modelPartBuilder,
                ModelTransform.of(0F, 16.0F, 16, (float) Math.PI, 0.0F, 0.0F)
        );
        return TexturedModelData.of(modelData, 16, 16);
    }

    public void render(
            StoragePotBlockEntity storagePotBlockEntity, float f, MatrixStack matrixStack,
            VertexConsumerProvider vertexConsumerProvider, int light, int overlay, Vec3d vec3d
    ) {
        matrixStack.push();
        Direction direction = storagePotBlockEntity.getHorizontalFacing();
        matrixStack.translate(0.5, 0.0, 0.5);
        matrixStack.multiply(
                RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - direction.getPositiveHorizontalDegrees()));
        matrixStack.translate(-0.5, 0.0, -0.5);
        DecoratedPotBlockEntity.WobbleType wobbleType = storagePotBlockEntity.lastWobbleType;

        World world = storagePotBlockEntity.getWorld();

        if (wobbleType != null && world != null) {
            float g = ((float) (world.getTime() - storagePotBlockEntity.lastWobbleTime) + f) / wobbleType.lengthInTicks;
            if (g >= 0.0F && g <= 1.0F) {
                if (wobbleType == DecoratedPotBlockEntity.WobbleType.POSITIVE) {
                    float h = 0.015625F;
                    float k = g * (float) (Math.PI * 2);
                    float l = -1.5F * (MathHelper.cos(k) + 0.5F) * MathHelper.sin(k / 2.0F);
                    matrixStack.multiply(RotationAxis.POSITIVE_X.rotation(l * h), 0.5F, 0.0F, 0.5F);
                    float m = MathHelper.sin(k);
                    matrixStack.multiply(RotationAxis.POSITIVE_Z.rotation(m * h), 0.5F, 0.0F, 0.5F);
                } else {
                    float h = MathHelper.sin(-g * 3.0F * (float) Math.PI) * 0.125F;
                    float k = 1.0F - g;
                    matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(h * k), 0.5F, 0.0F, 0.5F);
                }
            }
        }

        StoragePotContents contents = storagePotBlockEntity.getContents();
        ItemStack waxedItem = storagePotBlockEntity.getWaxedItem();

        this.render(matrixStack, vertexConsumerProvider, light, overlay, contents, waxedItem);
        this.renderText(matrixStack, vertexConsumerProvider, light, storagePotBlockEntity);

        matrixStack.pop();
    }

    public void render(
            MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int light, int overlay,
            StoragePotContents contents, ItemStack waxedItem
    ) {
        if (!waxedItem.isEmpty()) {
            contents = StoragePotContents.from(List.of(waxedItem));
        }

        VertexConsumer vertexConsumer = SPTexturedRenderLayers.STORAGE_POT_BASE.getVertexConsumer(
                vertexConsumers, RenderLayer::getEntitySolid);
        this.neck.render(matrixStack, vertexConsumer, light, overlay);
        this.top.render(matrixStack, vertexConsumer, light, overlay);
        this.bottom.render(matrixStack, vertexConsumer, light, overlay);

        this.renderSide(this.back, matrixStack, vertexConsumers, light, overlay);
        this.renderSide(this.left, matrixStack, vertexConsumers, light, overlay);
        this.renderSide(this.right, matrixStack, vertexConsumers, light, overlay);
        this.renderSide(
                this.front, matrixStack, vertexConsumers, light, overlay,
                SPTexturedRenderLayers.STORAGE_POT_FRONT
        );

        this.renderItem(matrixStack, vertexConsumers, overlay, light, contents);
    }

    private void renderSide(
            ModelPart part,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            int overlay
    ) {
        renderSide(part, matrices, vertexConsumers, light, overlay, SPTexturedRenderLayers.STORAGE_POT_SIDE);
    }

    private void renderSide(
            ModelPart part,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            int overlay
            , SpriteIdentifier textureId
    ) {
        part.render(
                matrices, textureId.getVertexConsumer(vertexConsumers, RenderLayer::getEntitySolid), light,
                overlay
        );
    }

    private void renderItem(
            MatrixStack matrices, VertexConsumerProvider vertexConsumers, int overlay, int light,
            StoragePotContents contents
    ) {
        float scale = 0.35F;

        matrices.push();
        matrices.translate(0.5F, 0.5F, 1F);
        matrices.scale(scale, scale, scale);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));

        this.itemRenderer.renderItem(
                contents.toStack(1), ItemDisplayContext.FIXED, light, overlay, matrices,
                vertexConsumers, null, 0
        );

        matrices.pop();
    }

    private void renderText(
            MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
            StoragePotBlockEntity storagePotBlockEntity
    ) {
        if (this.textRenderer != null) {
            int count = storagePotBlockEntity.getContents().count();
            int color = storagePotBlockEntity.isFull() ? Colors.RED : Colors.BLACK;
            String text = Integer.toString(count);

            if (count > 0) {
                float textWidth = textRenderer.getWidth(text);
                float halfWidth = textWidth / 2;
                float textHeight = 20;
                float scale = 0.0075F;

                matrices.push();
                matrices.translate(0.5F, 1F - scale * ((textHeight - 4) / 2), 1F);
                matrices.scale(scale, -scale, scale);

                this.textRenderer.draw(
                        Text.of(text), -halfWidth, textHeight, color, false,
                        matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.POLYGON_OFFSET,
                        0, light
                );

                matrices.pop();
            }
        }
    }

    public void collectVertices(Set<Vector3f> vertices) {
        MatrixStack matrixStack = new MatrixStack();
        this.neck.collectVertices(matrixStack, vertices);
        this.top.collectVertices(matrixStack, vertices);
        this.bottom.collectVertices(matrixStack, vertices);
    }
}