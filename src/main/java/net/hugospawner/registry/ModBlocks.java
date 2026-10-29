package net.hugospawner.registry;

import net.hugospawner.HugoSpawnerMod;
import net.hugospawner.block.SpawnerBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModBlocks {

    private static final Map<String, SpawnerBlock> SPAWNER_BLOCKS = new LinkedHashMap<>();

    public static SpawnerBlock get(String typeId) {
        SpawnerBlock block = SPAWNER_BLOCKS.get(typeId);
        if (block == null) {
            throw new IllegalArgumentException("Kein Spawner-Block fuer typeId " + typeId);
        }
        return block;
    }

    public static void register() {
        for (String typeId : ModItems.TYPE_IDS) {
            Identifier id = Identifier.of(HugoSpawnerMod.MOD_ID, typeId + "_spawner");
            RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, id);

            SpawnerBlock block = new SpawnerBlock(
                    typeId,
                    AbstractBlock.Settings.create()
                            .registryKey(key)
                            .mapColor(MapColor.PURPLE)
                            .strength(5.0f, 1200.0f)
                            .sounds(BlockSoundGroup.METAL)
                            .nonOpaque()
                            .luminance(state -> 7)
            );

            Registry.register(Registries.BLOCK, id, block);
            SPAWNER_BLOCKS.put(typeId, block);
        }
    }
}
