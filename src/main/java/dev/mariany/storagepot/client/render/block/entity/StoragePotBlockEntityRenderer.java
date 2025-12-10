package dev.mariany.storagepot.client.render.block.entity;

import dev.mariany.storagepot.block.SPBlocks;
import dev.mariany.storagepot.block.StoragePotBlock;
import dev.mariany.storagepot.block.entity.StoragePotBlockEntity;
import dev.mariany.storagepot.block.entity.StoragePotContents;
import dev.mariany.storagepot.client.render.SPTexturedRenderLayers;
import dev.mariany.storagepot.client.render.block.entity.state.StoragePotBlockEntityRenderState;
import dev.mariany.storagepot.client.render.entity.SPModelLayers;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.model.*;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.SpriteHolder;
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
import java.util.Set;

public class StoragePotBlockEntityRenderer
        implements BlockEntityRenderer<StoragePotBlockEntity, StoragePotBlockEntityRenderState> {
    private static final Direction DEFAULT_DIRECTION = Direction.SOUTH;

    private static final String FRONT = "front";
    private static final String BACK = "back";
    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final String TOP = "top";

    private final ModelPart neck;
    private final ModelPart front;
    private final ModelPart back;
    private final ModelPart left;
    private final ModelPart right;
    private final ModelPart top;
    private final ModelPart bottom;

    @Nullable
    private final TextRenderer textRenderer;
    private final ItemModelManager itemModelManager;
    private final SpriteHolder materials;

    public StoragePotBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this(context.itemModelManager(), context.loadedEntityModels(), context.spriteHolder(), context.textRenderer());
    }

    public StoragePotBlockEntityRenderer(
            ItemModelManager itemModelManager,
            LoadedEntityModels models,
            SpriteHolder materials
    ) {
        this(itemModelManager, models, materials, null);
    }

    public StoragePotBlockEntityRenderer(
            ItemModelManager itemModelManager,
            LoadedEntityModels models,
            SpriteHolder materials,
            @Nullable TextRenderer textRenderer
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

        this.itemModelManager = itemModelManager;
        this.textRenderer = textRenderer;
        this.materials = materials;
    }

    public static TexturedModelData getTopBottomNeckTexturedModelData() {
        ModelData modelData = new ModelData();

        Dilation dilation = new Dilation(0.2F);
        Dilation dilation2 = new Dilation(-0.1F);

        ModelPartData modelPartData = modelData.getRoot();

        modelPartData.addChild(
                EntityModelPartNames.NECK,
                ModelPartBuilder.create()
                                .uv(0, 0)
                                .cuboid(4F, 17F, 4F, 8F, 3F, 8F, dilation2)
                                .uv(0, 5)
                                .cuboid(5F, 20F, 5F, 6F, 1F, 6F, dilation),
                ModelTransform.of(0.0F, 37.0F, 16.0F, (float) Math.PI, 0.0F, 0.0F)
        );

        ModelPartBuilder modelPartBuilder = ModelPartBuilder.create()
                                                            .uv(-16, 13)
                                                            .cuboid(
                                                                    0F,
                                                                    0F,
                                                                    0F,
                                                                    16F,
                                                                    0F,
                                                                    16F
                                                            );

        modelPartData.addChild(
                "top",
                modelPartBuilder,
                ModelTransform.of(0.0F, 16.0F, 0.0F, 0.0F, 0.0F, 0.0F)
        );

        modelPartData.addChild(
                EntityModelPartNames.BOTTOM,
                modelPartBuilder,
                ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
        );

        return TexturedModelData.of(modelData, 32, 32);
    }

    public static TexturedModelData getSidesTexturedModelData() {
        ModelData modelData = new ModelData();

        ModelPartData modelPartData = modelData.getRoot();

        ModelPartBuilder modelPartBuilder = ModelPartBuilder.create()
                                                            .uv(0, 0)
                                                            .cuboid(
                                                                    0.0F,
                                                                    0.0F,
                                                                    0.0F,
                                                                    16.0F,
                                                                    16.0F,
                                                                    0.0F,
                                                                    EnumSet.of(Direction.NORTH)
                                                            );

        modelPartData.addChild(
                "back",
                modelPartBuilder,
                ModelTransform.of(16, 16F, 0F, 0F, 0F, (float) Math.PI)
        );

        modelPartData.addChild(
                "left",
                modelPartBuilder,
                ModelTransform.of(0F, 16F, 0F, 0F, (float) (-Math.PI / 2), (float) Math.PI)
        );

        modelPartData.addChild(
                "right", modelPartBuilder,
                ModelTransform.of(16, 16F, 16, 0F, (float) (Math.PI / 2), (float) Math.PI)
        );

        modelPartData.addChild(
                "front", modelPartBuilder,
                ModelTransform.of(0F, 16F, 16, (float) Math.PI, 0F, 0F)
        );

        return TexturedModelData.of(modelData, 16, 16);
    }

    public void collectVertices(Set<Vector3f> vertices) {
        MatrixStack matrixStack = new MatrixStack();
        this.neck.collectVertices(matrixStack, vertices);
        this.top.collectVertices(matrixStack, vertices);
        this.bottom.collectVertices(matrixStack, vertices);
    }

    @Override
    public StoragePotBlockEntityRenderState createRenderState() {
        return new StoragePotBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(
            StoragePotBlockEntity storagePotBlockEntity,
            StoragePotBlockEntityRenderState storagePotBlockEntityRenderState,
            float tickProgress,
            Vec3d cameraPos,
            @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        BlockEntityRenderer.super.updateRenderState(
                storagePotBlockEntity,
                storagePotBlockEntityRenderState,
                tickProgress,
                cameraPos,
                crumblingOverlay
        );

        this.updateRenderState(
                storagePotBlockEntityRenderState,
                storagePotBlockEntity.getContents(),
                storagePotBlockEntity.getWaxedItem(),
                storagePotBlockEntity.isFull(),
                this.getFacing(storagePotBlockEntity),
                storagePotBlockEntity.getWorld()
        );

        this.updateWobbleState(storagePotBlockEntityRenderState, storagePotBlockEntity, tickProgress);
    }

    private Direction getFacing(StoragePotBlockEntity storagePotBlockEntity) {
        BlockState blockState;

        if (storagePotBlockEntity.getWorld() == null) {
            blockState = SPBlocks.STORAGE_POT.getDefaultState().with(StoragePotBlock.FACING, DEFAULT_DIRECTION);
        } else {
            blockState = storagePotBlockEntity.getCachedState();
        }

        return blockState.get(StoragePotBlock.FACING, DEFAULT_DIRECTION);
    }

    private void updateWobbleState(
            StoragePotBlockEntityRenderState storagePotBlockEntityRenderState,
            StoragePotBlockEntity storagePotBlockEntity,
            float tickProgress
    ) {
        DecoratedPotBlockEntity.WobbleType wobbleType = storagePotBlockEntity.lastWobbleType;

        if (wobbleType != null && storagePotBlockEntity.getWorld() != null) {
            long worldTime = storagePotBlockEntity.getWorld().getTime();
            long elapsedTicks = worldTime - storagePotBlockEntity.lastWobbleTime;

            float totalProgress = elapsedTicks + tickProgress;
            float wobbleDuration = wobbleType.lengthInTicks;

            storagePotBlockEntityRenderState.wobbleAnimationProgress = totalProgress / wobbleDuration;
        } else {
            storagePotBlockEntityRenderState.wobbleAnimationProgress = 0;
        }
    }

    public void updateRenderState(
            StoragePotBlockEntityRenderState storagePotBlockEntityRenderState,
            StoragePotContents contents,
            ItemStack waxedItem
    ) {
        updateRenderState(
                storagePotBlockEntityRenderState,
                contents,
                waxedItem,
                false,
                DEFAULT_DIRECTION,
                null
        );
    }

    public void updateRenderState(
            StoragePotBlockEntityRenderState storagePotBlockEntityRenderState,
            StoragePotContents contents,
            ItemStack waxedItem,
            boolean full,
            Direction facing,
            @Nullable World world
    ) {
        storagePotBlockEntityRenderState.count = contents.count();
        storagePotBlockEntityRenderState.full = full;
        storagePotBlockEntityRenderState.yaw = facing.getPositiveHorizontalDegrees();

        this.updateStackState(storagePotBlockEntityRenderState, contents, waxedItem, world);
    }

    private void updateStackState(
            StoragePotBlockEntityRenderState storagePotBlockEntityRenderState,
            StoragePotContents contents,
            ItemStack waxedItem,
            @Nullable World world
    ) {
        ItemStack contentStack;

        if (waxedItem.isEmpty()) {
            contentStack = contents.toStack(1);
        } else {
            contentStack = waxedItem;
        }

        this.itemModelManager.clearAndUpdate(
                storagePotBlockEntityRenderState.itemRenderState,
                contentStack,
                ItemDisplayContext.FIXED,
                world,
                null,
                0
        );
    }

    @Override
    public void render(
            StoragePotBlockEntityRenderState storagePotBlockEntityRenderState,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            CameraRenderState cameraState
    ) {
        matrices.push();
        matrices.translate(0.5, 0.0, 0.5);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180 - storagePotBlockEntityRenderState.yaw));
        matrices.translate(-0.5, 0.0, -0.5);

        this.wobble(storagePotBlockEntityRenderState, matrices);

        this.render(
                storagePotBlockEntityRenderState,
                matrices,
                queue,
                storagePotBlockEntityRenderState.lightmapCoordinates,
                OverlayTexture.DEFAULT_UV,
                0
        );

        this.renderText(storagePotBlockEntityRenderState, matrices, queue);

        matrices.pop();
    }

    private void wobble(
            StoragePotBlockEntityRenderState storagePotBlockEntityRenderState,
            MatrixStack matrices
    ) {
        float progress = storagePotBlockEntityRenderState.wobbleAnimationProgress;

        if (progress >= 0 && progress <= 1) {
            if (storagePotBlockEntityRenderState.wobbleType == DecoratedPotBlockEntity.WobbleType.POSITIVE) {
                float scale = 0.015625F;
                float angle = progress * (float) (Math.PI * 2);
                float tiltX = -1.5F * (MathHelper.cos(angle) + 0.5F) * MathHelper.sin(angle / 2);

                matrices.multiply(
                        RotationAxis.POSITIVE_X.rotation(tiltX * scale),
                        0.5F,
                        0,
                        0.5F
                );

                float tiltZ = MathHelper.sin(angle);

                matrices.multiply(
                        RotationAxis.POSITIVE_Z.rotation(tiltZ * scale),
                        0.5F,
                        0,
                        0.5F
                );
            } else {
                float yaw = MathHelper.sin(-progress * 3F * (float) Math.PI) * 0.125F;
                float dampen = 1 - progress;

                matrices.multiply(
                        RotationAxis.POSITIVE_Y.rotation(yaw * dampen),
                        0.5F,
                        0,
                        0.5F
                );
            }
        }
    }

    public void render(
            StoragePotBlockEntityRenderState storagePotBlockEntityRenderState,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            int light,
            int overlay,
            int outlineColor
    ) {
        this.renderBase(this.neck, matrices, queue, light, overlay, outlineColor);
        this.renderBase(this.top, matrices, queue, light, overlay, outlineColor);
        this.renderBase(this.bottom, matrices, queue, light, overlay, outlineColor);

        this.renderSide(this.back, matrices, queue, light, overlay, outlineColor);
        this.renderSide(this.left, matrices, queue, light, overlay, outlineColor);
        this.renderSide(this.right, matrices, queue, light, overlay, outlineColor);

        this.renderPart(
                SPTexturedRenderLayers.STORAGE_POT_FRONT,
                this.front,
                matrices,
                queue,
                light,
                overlay,
                outlineColor
        );

        this.renderItem(storagePotBlockEntityRenderState, light, matrices, queue);
    }

    private void renderBase(
            ModelPart part,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            int light,
            int overlay,
            int outlineColor
    ) {
        renderPart(SPTexturedRenderLayers.STORAGE_POT_BASE, part, matrices, queue, light, overlay, outlineColor);
    }

    private void renderSide(
            ModelPart part,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            int light,
            int overlay,
            int outlineColor
    ) {
        renderPart(SPTexturedRenderLayers.STORAGE_POT_SIDE, part, matrices, queue, light, overlay, outlineColor);
    }

    private void renderPart(
            SpriteIdentifier textureId,
            ModelPart part,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            int light,
            int overlay,
            int outlineColor
    ) {
        queue.submitModelPart(
                part,
                matrices,
                textureId.getRenderLayer(RenderLayer::getEntitySolid),
                light,
                overlay,
                this.materials.getSprite(textureId),
                false,
                false,
                -1,
                null,
                outlineColor
        );
    }

    private void renderItem(
            StoragePotBlockEntityRenderState storagePotBlockEntityRenderState,
            int light,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue
    ) {
        float scale = 0.35F;

        matrices.push();
        matrices.translate(0.5F, 0.5F, 1F);
        matrices.scale(scale, scale, scale);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));

        storagePotBlockEntityRenderState.itemRenderState.render(
                matrices,
                queue,
                light,
                OverlayTexture.DEFAULT_UV,
                0
        );

        matrices.pop();
    }

    private void renderText(
            StoragePotBlockEntityRenderState storagePotBlockEntityRenderState,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue
    ) {
        if (this.textRenderer != null) {
            int count = storagePotBlockEntityRenderState.count;
            int color = storagePotBlockEntityRenderState.full ? Colors.RED : Colors.BLACK;
            String text = Integer.toString(count);

            if (count > 0) {
                float textWidth = textRenderer.getWidth(text);
                float halfWidth = textWidth / 2;
                float textHeight = 20;
                float scale = 0.0075F;

                matrices.push();
                matrices.translate(0.5F, 1F - scale * ((textHeight - 4) / 2), 1F);
                matrices.scale(scale, -scale, scale);

                queue.submitText(
                        matrices,
                        -halfWidth,
                        textHeight,
                        Text.of(text).asOrderedText(),
                        false,
                        TextRenderer.TextLayerType.POLYGON_OFFSET,
                        storagePotBlockEntityRenderState.lightmapCoordinates,
                        color,
                        0,
                        0
                );

                matrices.pop();
            }
        }
    }
}