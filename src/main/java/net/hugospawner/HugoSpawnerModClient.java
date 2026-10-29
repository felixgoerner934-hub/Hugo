package net.hugospawner;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.hugospawner.registry.ModScreenHandlers;
import net.hugospawner.screen.SpawnerScreen;
import net.hugospawner.screen.SpawnerShopScreen;

public class HugoSpawnerModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.SPAWNER_SCREEN_HANDLER, SpawnerScreen::new);
        HandledScreens.register(ModScreenHandlers.SPAWNER_SHOP_SCREEN_HANDLER, SpawnerShopScreen::new);
    }
}
