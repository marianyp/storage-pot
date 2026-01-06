package dev.mariany.storagepot.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.ComponentChanges;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.dynamic.Codecs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public record StoragePotContents(RegistryEntry<Item> item, int count, ComponentChanges components) {
    public static final Codec<StoragePotContents> CODEC = Codec.lazyInitialized(StoragePotContents::createCodec);
    public static final PacketCodec<RegistryByteBuf, StoragePotContents> PACKET_CODEC = createPacketCodec();

    public static StoragePotContents EMPTY = new StoragePotContents(
            Items.AIR.getDefaultStack().getRegistryEntry(),
            0,
            ComponentChanges.EMPTY
    );

    private static Codec<StoragePotContents> createCodec() {
        return RecordCodecBuilder.create(instance -> instance.group(
                Registries.ITEM.getEntryCodec().fieldOf("id").forGetter(StoragePotContents::item),
                Codecs.NON_NEGATIVE_INT.fieldOf("count").orElse(1).forGetter(StoragePotContents::count),
                ComponentChanges.CODEC.optionalFieldOf("components", ComponentChanges.EMPTY)
                                      .forGetter(StoragePotContents::components)
        ).apply(instance, StoragePotContents::new));
    }

    private static PacketCodec<RegistryByteBuf, StoragePotContents> createPacketCodec() {
        return PacketCodec.tuple(
                Item.ENTRY_PACKET_CODEC, StoragePotContents::item,
                PacketCodecs.VAR_INT, StoragePotContents::count,
                ComponentChanges.PACKET_CODEC, StoragePotContents::components,
                StoragePotContents::new
        );
    }

    public static StoragePotContents from(List<ItemStack> stacks) {
        if (stacks.isEmpty()) {
            return EMPTY;
        }

        ItemStack baseStack = stacks.getFirst();
        RegistryEntry<Item> baseItem = baseStack.getRegistryEntry();
        ComponentChanges componentChanges = baseStack.getComponentChanges();

        if (!stacks.stream().allMatch(stack -> stack.getItem().equals(baseStack.getItem()))) {
            throw new IllegalArgumentException("Stacks must all be of the same item.");
        }

        if (!stacks.stream().allMatch(stack -> stack.getComponentChanges().equals(componentChanges))) {
            throw new IllegalArgumentException("Stacks must have matching components.");
        }


        int count = 0;

        for (ItemStack stack : stacks) {
            count += stack.getCount();
        }

        return new StoragePotContents(baseItem, count, componentChanges);
    }

    public StoragePotContents withCount(Function<Integer, Integer> callback) {
        return withCount(callback.apply(this.count));
    }

    public StoragePotContents withCount(int newCount) {
        if (newCount <= 0) {
            return EMPTY;
        }

        return new StoragePotContents(this.item, newCount, components);
    }

    public ItemStack toStack(int count) {
        if (count <= 0) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(this.item, count, this.components);
    }

    public boolean isEmpty() {
        return count <= 0 || item.value() == Items.AIR;
    }

    public List<ItemStack> toStacks() {
        List<ItemStack> stacks = new ArrayList<>();

        if (!this.isEmpty()) {
            ItemStack baseStack = this.toStack(1);
            int maxCountPerStack = baseStack.getMaxCount();
            int remaining = this.count;

            while (remaining > 0) {
                int stackSize = Math.min(remaining, maxCountPerStack);
                ItemStack splitStack = this.toStack(stackSize);
                stacks.add(splitStack);
                remaining -= stackSize;
            }
        }

        return stacks;
    }

    public ItemStack getBaseStack() {
        if (this.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return this.toStacks().getFirst();
    }
}
