package net.hugospawner.registry;

import net.hugospawner.HugoSpawnerMod;
import net.hugospawner.block.SpawnerBlock;
import net.hugospawner.block.SpawnerBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModBlockEntities {

    public static BlockEntityType<SpawnerBlockEntity> SPAWNER;

    public static void register() {
        Block[] blocks = new Block[ModItems.TYPE_IDS.length];
        for (int i = 0; i < ModItems.TYPE_IDS.length; i++) {
            blocks[i] = ModBlocks.get(ModItems.TYPE_IDS[i]);
        }

        SPAWNER = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(HugoSpawnerMod.MOD_ID, "spawner"),
                BlockEntityType.Builder.create(
                        // typeId wird aus dem tatsaechlichen Block abgeleitet, nicht geraten -
                        // wichtig fuer korrektes Laden nach Weltneustart.
                        (pos, state) -> new SpawnerBlockEntity(pos, state, ((SpawnerBlock) state.getBlock()).getTypeId()),
                        blocks
                ).build()
        );
    }
}
