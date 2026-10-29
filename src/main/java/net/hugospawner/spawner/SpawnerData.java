package net.hugospawner.spawner;

import net.minecraft.item.Item;

import java.util.List;

/**
 * Eigenschaften eines einzelnen Spawner-Typs (z.B. "Skelett").
 * Wird zur Laufzeit aus data/hugospawner/spawner_types.json geladen,
 * damit Preise/Drops ohne Neukompilieren angepasst werden koennen.
 */
public record SpawnerData(
        String id,
        String displayName,
        int priceGems,
        int baseStorageSlots,
        int storageSlotsPerExtraStack,
        boolean confirmed,
        List<WeightedDrop> drops
) {
    public record WeightedDrop(Item item, int weight) {
    }

    public int storageSlotsForStack(int stackCount) {
        return baseStorageSlots + Math.max(0, stackCount - 1) * storageSlotsPerExtraStack;
    }
}
