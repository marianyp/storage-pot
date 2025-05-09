package dev.mariany.storagepot.item.component;

import dev.mariany.storagepot.StoragePot;
import dev.mariany.storagepot.block.entity.StoragePotContents;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class SPComponents {
    public static final ComponentType<StoragePotContents> CONTENTS = register("contents",
            ComponentType.<StoragePotContents>builder().codec(StoragePotContents.CODEC)
                    .packetCodec(StoragePotContents.PACKET_CODEC));

    public static final ComponentType<ItemStack> WAXED = register("waxed",
            ComponentType.<ItemStack>builder().codec(ItemStack.CODEC).packetCodec(ItemStack.OPTIONAL_PACKET_CODEC)
                    .cache());

    private static <T> ComponentType<T> register(String name, ComponentType.Builder<T> builder) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, StoragePot.id(name), builder.build());
    }

    public static void bootstrap() {
        StoragePot.LOGGER.info("Registering components for " + StoragePot.MOD_ID);
    }
}
