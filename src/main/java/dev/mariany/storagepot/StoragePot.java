package dev.mariany.storagepot;

import dev.mariany.storagepot.block.SPBlocks;
import dev.mariany.storagepot.block.entity.SPBlockEntities;
import dev.mariany.storagepot.event.block.AttackBlockHandler;
import dev.mariany.storagepot.event.item.UseItemHandler;
import dev.mariany.storagepot.item.component.SPComponents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoragePot implements ModInitializer {
    public static final String MOD_ID = "storagepot";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        SPComponents.bootstrap();
        SPBlocks.bootstrap();
        SPBlockEntities.bootstrap();

        UseItemCallback.EVENT.register(UseItemHandler::onInteract);
        AttackBlockCallback.EVENT.register(AttackBlockHandler::onAttack);
    }

    public static Identifier id(String resource) {
        return Identifier.of(MOD_ID, resource);
    }
}