package net.hugospawner;

import net.fabricmc.api.ModInitializer;
import net.hugospawner.command.SpawnerShopCommand;
import net.hugospawner.economy.GemManager;
import net.hugospawner.registry.ModBlockEntities;
import net.hugospawner.registry.ModBlocks;
import net.hugospawner.registry.ModItems;
import net.hugospawner.registry.ModScreenHandlers;
import net.hugospawner.spawner.SpawnerDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HugoSpawnerMod implements ModInitializer {

    public static final String MOD_ID = "hugospawner";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("HugoSpawner-Mod startet");

        SpawnerDataLoader.register();

        ModBlocks.register();
        ModItems.register();
        ModBlockEntities.register();
        ModScreenHandlers.register();

        SpawnerShopCommand.register();
        GemManager.register();
    }
}
