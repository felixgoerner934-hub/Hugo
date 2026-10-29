package net.hugospawner.registry;

import net.hugospawner.HugoSpawnerMod;
import net.hugospawner.screen.SpawnerScreenHandler;
import net.hugospawner.screen.SpawnerShopScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public final class ModScreenHandlers {

    public static ScreenHandlerType<SpawnerScreenHandler> SPAWNER_SCREEN_HANDLER;
    public static ScreenHandlerType<SpawnerShopScreenHandler> SPAWNER_SHOP_SCREEN_HANDLER;

    public static void register() {
        SPAWNER_SCREEN_HANDLER = Registry.register(
                Registries.SCREEN_HANDLER,
                Identifier.of(HugoSpawnerMod.MOD_ID, "spawner"),
                new ScreenHandlerType<>(SpawnerScreenHandler::new, null)
        );

        SPAWNER_SHOP_SCREEN_HANDLER = Registry.register(
                Registries.SCREEN_HANDLER,
                Identifier.of(HugoSpawnerMod.MOD_ID, "spawner_shop"),
                new ScreenHandlerType<>(SpawnerShopScreenHandler::new, null)
        );
    }
}
