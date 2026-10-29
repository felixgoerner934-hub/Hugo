package net.hugospawner.economy;

import net.hugospawner.block.SpawnerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Sehr einfaches Gem-Guthabensystem, eigenstaendig (nicht an echtes Geld
 * gekoppelt - das Gem-Kauf-Angebot aus dem HugoSMP-Webshop ist bewusst NICHT
 * nachgebaut, nur die Ingame-Waehrung fuer Spawner-Kauf/-Verkauf).
 *
 * FILL: Verkaufspreise pro Drop-Item sind noch nicht aus HugoSMP bestaetigt.
 * SELL_PRICE_PER_ITEM_PLACEHOLDER gilt fuer ALLE Items, bis du echte Werte hast.
 */
public final class GemManager {

    private static final int SELL_PRICE_PER_ITEM_PLACEHOLDER = 1;

    private GemManager() {
    }

    public static void register() {
        // Platzhalter fuer spaetere Events (z.B. Server-Join-Nachricht mit Guthaben).
    }

    public static long getBalance(ServerWorld world, UUID playerId) {
        return state(world).balances.getOrDefault(playerId, 0L);
    }

    public static void addBalance(ServerWorld world, UUID playerId, long amount) {
        State s = state(world);
        s.balances.merge(playerId, amount, Long::sum);
        s.markDirty();
    }

    public static boolean tryCharge(ServerWorld world, UUID playerId, long amount) {
        State s = state(world);
        long current = s.balances.getOrDefault(playerId, 0L);
        if (current < amount) return false;
        s.balances.put(playerId, current - amount);
        s.markDirty();
        return true;
    }

    public static void sellDrops(PlayerEntity player, SpawnerBlockEntity ignoredContext, ItemStack stack) {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return;
        long gain = (long) stack.getCount() * SELL_PRICE_PER_ITEM_PLACEHOLDER;
        addBalance(serverWorld, player.getUuid(), gain);
        player.sendMessage(Text.literal("§d+" + gain + " Gems"), true);
    }

    private static State state(ServerWorld world) {
        PersistentStateManager manager = world.getServer().getOverworld().getPersistentStateManager();
        return manager.getOrCreate(State.TYPE, "hugospawner_gems");
    }

    private static final class State extends PersistentState {

        final Map<UUID, Long> balances = new HashMap<>();

        static final Type<State> TYPE = new Type<>(State::new, State::fromNbt, null);

        static State fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
            State s = new State();
            NbtCompound map = nbt.getCompound("balances");
            for (String key : map.getKeys()) {
                s.balances.put(UUID.fromString(key), map.getLong(key));
            }
            return s;
        }

        @Override
        public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
            NbtCompound map = new NbtCompound();
            for (var entry : balances.entrySet()) {
                map.putLong(entry.getKey().toString(), entry.getValue());
            }
            nbt.put("balances", map);
            return nbt;
        }
    }
}
