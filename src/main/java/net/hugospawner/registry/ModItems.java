package net.hugospawner.registry;

import net.hugospawner.HugoSpawnerMod;
import net.hugospawner.item.SpawnerItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ein BlockItem pro Spawner-Typ. Muss NACH ModBlocks.register() aufgerufen werden,
 * weil jedes Item sein Block-Gegenstueck braucht.
 */
public final class ModItems {

    public static final String[] TYPE_IDS = {
            "skelett", "creeper", "spinne", "kuh", "golem", "piglin", "lohe"
    };

    private static final Map<String, Item> SPAWNER_ITEMS = new LinkedHashMap<>();

    public static Item get(String typeId) {
        Item item = SPAWNER_ITEMS.get(typeId);
        if (item == null) {
            throw new IllegalArgumentException("Kein Spawner-Item fuer typeId " + typeId);
        }
        return item;
    }

    public static Map<String, Item> all() {
        return SPAWNER_ITEMS;
    }

    public static void register() {
        for (String typeId : TYPE_IDS) {
            Identifier id = Identifier.of(HugoSpawnerMod.MOD_ID, typeId + "_spawner");
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);

            SpawnerItem item = new SpawnerItem(
                    typeId,
                    ModBlocks.get(typeId),
                    new Item.Settings().registryKey(key).maxCount(64)
            );

            Registry.register(Registries.ITEM, id, item);
            SPAWNER_ITEMS.put(typeId, item);
        }
    }
}
