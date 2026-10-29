package net.hugospawner.command;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.hugospawner.screen.SpawnerShopScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public final class SpawnerShopCommand {

    private SpawnerShopCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("spawner").executes(SpawnerShopCommand::execute)));
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) return 0;

        player.openHandledScreen(new NamedScreenHandlerFactory() {
            @Override
            public Text getDisplayName() {
                return Text.literal("Spawner Shop");
            }

            @Override
            public net.minecraft.screen.ScreenHandler createMenu(int syncId,
                    net.minecraft.entity.player.PlayerInventory inv, net.minecraft.entity.player.PlayerEntity p) {
                return new SpawnerShopScreenHandler(syncId, inv);
            }
        });
        return 1;
    }
}
