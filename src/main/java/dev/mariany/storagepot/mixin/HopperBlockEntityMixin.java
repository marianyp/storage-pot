package dev.mariany.storagepot.mixin;

import dev.mariany.storagepot.block.entity.StoragePotBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {
    @Inject(
            method = "transfer(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/item/ItemStack;ILnet/minecraft/util/math/Direction;)Lnet/minecraft/item/ItemStack;",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private static void injectTransfer(
            Inventory from, Inventory to, ItemStack stack, int slot, Direction side,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (to instanceof StoragePotBlockEntity storagePotBlockEntity) {
            storagePotBlockEntity.add(null, stack, true);
            cir.setReturnValue(stack);
        }
    }
}
