package net.hugospawner.block;

import net.hugospawner.registry.ModBlockEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

/**
 * Spawner-Block fuer genau einen Mob-Typ (z.B. "skelett").
 * Rechtsklick mit einem passenden Spawner-Item stackt den Block,
 * jeder andere Rechtsklick oeffnet das Loot-Menue (SpawnerScreenHandler).
 */
public class SpawnerBlock extends BlockWithEntity implements BlockEntityProvider {

    private final String typeId;

    public SpawnerBlock(String typeId, Settings settings) {
        super(settings);
        this.typeId = typeId;
    }

    public String getTypeId() {
        return typeId;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockEntity createBlockEntity(net.minecraft.util.math.BlockPos pos, BlockState state) {
        return new SpawnerBlockEntity(pos, state, typeId);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, ModBlockEntities.SPAWNER, SpawnerBlockEntity::serverTick);
    }

    // ACHTUNG: Die Signatur von Block#onUse wurde zwischen 1.20 und 1.21.x mehrfach
    // umgebaut (Aufspaltung in onUseWithItem/onUse). Pruefe beim ersten Kompilieren
    // gegen die 1.21.11-Yarn-Mappings, ob diese Methode noch exakt so heisst
    // (die IDE zeigt dir sonst "method does not override" an und listet die
    // tatsaechlich verfuegbare Signatur).
    @Override
    protected ActionResult onUse(BlockState state, World world, net.minecraft.util.math.BlockPos pos,
                                  PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        ItemStack held = player.getStackInHand(Hand.MAIN_HAND);
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof SpawnerBlockEntity spawnerBe)) {
            return ActionResult.PASS;
        }

        boolean isMatchingSpawnerItem = held.getItem() instanceof net.hugospawner.item.SpawnerItem si
                && si.getTypeId().equals(typeId);

        if (isMatchingSpawnerItem) {
            spawnerBe.increaseStack(1);
            if (!player.isCreative()) {
                held.decrement(1);
            }
            world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_AMETHYST_BLOCK_PLACE,
                    net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, 1.2f);
            return ActionResult.CONSUME;
        }

        if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
            spawnerBe.openScreen(serverPlayer);
        }
        return ActionResult.CONSUME;
    }
}
