package dev.mariany.storagepot.event.item;

import dev.mariany.storagepot.block.entity.StoragePotContents;
import dev.mariany.storagepot.item.component.SPComponents;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ItemTooltipHandler {
    public static void getTooltip(
            ItemStack stack,
            Item.TooltipContext tooltipContext,
            TooltipType tooltipType,
            List<Text> texts
    ) {
        ComponentMap components = stack.getComponents();
        StoragePotContents contents = components.getOrDefault(SPComponents.CONTENTS, StoragePotContents.EMPTY);
        ItemStack storedItem = contents.getBaseStack();
        ItemStack waxedItem = components.getOrDefault(SPComponents.WAXED, ItemStack.EMPTY);
        boolean waxed = !waxedItem.isEmpty();

        if (storedItem.isEmpty()) {
            storedItem = waxedItem;
        }

        Text tooltip = null;

        if (!storedItem.isEmpty()) {
            Text name = storedItem.getName();
            int count = contents.count();

            tooltip = Text.translatable("block.storagepot.contents.tooltip", name, count).formatted(Formatting.GRAY);
        }

        if (waxed) {
            Text waxedTooltip = Text.of(Text.translatable("block.storagepot.waxed").formatted(Formatting.GOLD));

            if (tooltip != null) {
                tooltip = Text.of(tooltip.copy().append(" ").append(waxedTooltip));
            } else {
                tooltip = waxedTooltip;
            }
        }

        if (tooltip != null) {
            texts.add(tooltip);
        }
    }
}
