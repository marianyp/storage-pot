package dev.mariany.storagepot.client.render.block.entity.state;

import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import org.jetbrains.annotations.Nullable;

public class StoragePotBlockEntityRenderState extends BlockEntityRenderState {
    public final ItemRenderState itemRenderState = new ItemRenderState();
    public float yaw;
    public int count;
    public boolean full;
    public float wobbleAnimationProgress;
    @Nullable
    public DecoratedPotBlockEntity.WobbleType wobbleType;
}
