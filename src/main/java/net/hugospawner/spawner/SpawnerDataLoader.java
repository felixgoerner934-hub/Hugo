package net.hugospawner.spawner;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.hugospawner.HugoSpawnerMod;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Laedt data/hugospawner/spawner_types.json beim Server-/Datapack-Reload.
 * SpawnerDataLoader.get("skelett") liefert danach die geladenen Werte.
 */
public final class SpawnerDataLoader implements IdentifiableResourceReloadListener {

    private static final Gson GSON = new Gson();
    private static final Identifier FILE = Identifier.of(HugoSpawnerMod.MOD_ID, "spawner_types.json");

    private static Map<String, SpawnerData> current = Map.of();

    public static void register() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SpawnerDataLoader());
    }

    public static SpawnerData get(String id) {
        SpawnerData data = current.get(id);
        if (data == null) {
            throw new IllegalStateException("Unbekannter Spawner-Typ: " + id
                    + " (ist er in spawner_types.json definiert?)");
        }
        return data;
    }

    public static Map<String, SpawnerData> all() {
        return current;
    }

    @Override
    public Identifier getFabricId() {
        return FILE;
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager,
                                           Profiler prepareProfiler, Profiler applyProfiler,
                                           Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture.supplyAsync(() -> loadFromResources(manager), prepareExecutor)
                .thenCompose(synchronizer::whenPrepared)
                .thenAcceptAsync(loaded -> current = loaded, applyExecutor);
    }

    private Map<String, SpawnerData> loadFromResources(ResourceManager manager) {
        Map<String, SpawnerData> result = new LinkedHashMap<>();
        var resource = manager.getResource(
                Identifier.of(HugoSpawnerMod.MOD_ID, "spawner_types.json"));
        if (resource.isEmpty()) {
            HugoSpawnerMod.LOGGER.error("spawner_types.json nicht gefunden - keine Spawner-Typen geladen.");
            return result;
        }

        try (Reader reader = new InputStreamReader(resource.get().getInputStream())) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            for (String key : root.keySet()) {
                if (key.startsWith("_")) continue; // _readme etc.
                JsonObject entry = root.getAsJsonObject(key);
                result.put(key, parseEntry(key, entry));
            }
        } catch (Exception e) {
            HugoSpawnerMod.LOGGER.error("Konnte spawner_types.json nicht laden", e);
        }
        return result;
    }

    private SpawnerData parseEntry(String id, JsonObject entry) {
        List<SpawnerData.WeightedDrop> drops = new ArrayList<>();
        JsonArray dropsArray = entry.getAsJsonArray("drops");
        for (var el : dropsArray) {
            JsonObject dropObj = el.getAsJsonObject();
            var item = Registries.ITEM.get(Identifier.of(dropObj.get("item").getAsString()));
            drops.add(new SpawnerData.WeightedDrop(item, dropObj.get("weight").getAsInt()));
        }

        return new SpawnerData(
                id,
                entry.get("displayName").getAsString(),
                entry.get("priceGems").getAsInt(),
                entry.get("baseStorageSlots").getAsInt(),
                entry.get("storageSlotsPerExtraStack").getAsInt(),
                entry.has("confirmed") && entry.get("confirmed").getAsBoolean(),
                drops
        );
    }
}
