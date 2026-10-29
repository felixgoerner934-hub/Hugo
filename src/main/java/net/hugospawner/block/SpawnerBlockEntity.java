package net.hugospawner.block;

import net.hugospawner.registry.ModBlockEntities;
import net.hugospawner.registry.ModScreenHandlers;
import net.hugospawner.spawner.SpawnerData;
import net.hugospawner.spawner.SpawnerDataLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;

/**
 * Pro-Block-Zustand eines Spawners: Mob-Typ (typeId), Stack-Anzahl,
 * aktuelles Loot-Lager (mehrseitig, 36 Slots je "Seite" wie im Tooltip
 * "Seite 1/1") und die Drop-Zaehler (z.B. Bones/Arrows beim Skelett).
 *
 * Erzeugt Items direkt im Tick, OHNE echte Mob-Entities zu spawnen -
 * das entspricht "Loot ohne echte Mobs" aus dem HugoSMP-Shop-Screenshot.
 */
public class SpawnerBlockEntity extends BlockEntity implements NamedScreenHandlerFactory {

    private static final int SLOTS_PER_PAGE = 36;
    // FILL: echtes HugoSMP-Produktionsintervall unbekannt. 100 Ticks = 5 Sekunden
    // pro Stack ist eine dokumentierte Annahme, bis du den echten Wert hast.
    private static final int TICKS_PER_PRODUCTION_BASE = 100;

    private final String typeId;
    private int stackCount = 1;
    private int tickCounter = 0;
    private SimpleInventory storage = new SimpleInventory(SLOTS_PER_PAGE);
    private int currentPage = 0;

    public SpawnerBlockEntity(BlockPos pos, BlockState state, String typeId) {
        super(ModBlockEntities.SPAWNER, pos, state);
        this.typeId = typeId;
    }

    private SpawnerData data() {
        return SpawnerDataLoader.get(typeId);
    }

    public void increaseStack(int amount) {
        this.stackCount += amount;
        resizeStorageIfNeeded();
        markDirty();
    }

    private void resizeStorageIfNeeded() {
        int totalSlots = data().storageSlotsForStack(stackCount);
        int pages = Math.max(1, (int) Math.ceil(totalSlots / (double) SLOTS_PER_PAGE));
        // Lager besteht aus "pages" Seiten a SLOTS_PER_PAGE Slots, intern als eine
        // grosse Inventory verwaltet und seitenweise ins Fenster projiziert.
        if (storage.size() != pages * SLOTS_PER_PAGE) {
            SimpleInventory bigger = new SimpleInventory(pages * SLOTS_PER_PAGE);
            for (int i = 0; i < storage.size(); i++) {
                bigger.setStack(i, storage.getStack(i));
            }
            storage = bigger;
        }
    }

    public int getStackCount() {
        return stackCount;
    }

    public int getTotalSlots() {
        return storage.size();
    }

    public int getPageCount() {
        return Math.max(1, storage.size() / SLOTS_PER_PAGE);
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setPage(int page) {
        this.currentPage = Math.max(0, Math.min(page, getPageCount() - 1));
    }

    /** "Auslastung": wie viele der Lagerplaetze aktuell belegt sind, 0-100. */
    public int getLoadPercent() {
        int used = 0;
        int total = storage.size();
        for (int i = 0; i < total; i++) {
            if (!storage.getStack(i).isEmpty()) used++;
        }
        return total == 0 ? 0 : (int) Math.round(used * 100.0 / total);
    }

    /** Slots der aktuell angezeigten Seite, fuer den ScreenHandler. */
    public SimpleInventory pageInventory() {
        return storage; // ScreenHandler liest ueber getStack(currentPage*36 + slot)
    }

    public static void serverTick(net.minecraft.world.World world, BlockPos pos, BlockState state, SpawnerBlockEntity be) {
        be.tickCounter++;
        int interval = Math.max(10, TICKS_PER_PRODUCTION_BASE / be.stackCount);
        if (be.tickCounter < interval) return;
        be.tickCounter = 0;
        be.produceLoot(world.getRandom());
    }

    private void produceLoot(Random random) {
        SpawnerData d = data();
        if (d.drops().isEmpty()) return;

        int totalWeight = d.drops().stream().mapToInt(SpawnerData.WeightedDrop::weight).sum();
        int roll = random.nextInt(Math.max(1, totalWeight));
        int acc = 0;
        for (SpawnerData.WeightedDrop drop : d.drops()) {
            acc += drop.weight();
            if (roll < acc) {
                insertIntoStorage(new ItemStack(drop.item(), stackCount));
                break;
            }
        }
        markDirty();
    }

    private void insertIntoStorage(ItemStack stack) {
        for (int i = 0; i < storage.size() && !stack.isEmpty(); i++) {
            ItemStack slot = storage.getStack(i);
            if (slot.isEmpty()) {
                storage.setStack(i, stack);
                return;
            } else if (ItemStack.areItemsAndComponentsEqual(slot, stack)
                    && slot.getCount() < slot.getMaxCount()) {
                int space = slot.getMaxCount() - slot.getCount();
                int move = Math.min(space, stack.getCount());
                slot.increment(move);
                stack.decrement(move);
            }
        }
        // Lager voll: ueberschuessiger Loot geht verloren (bewusst, siehe "Auslastung" = Cap).
    }

    /** Leert die komplette, aktuell angezeigte Seite als Drops in die Welt ("Dropper"-Knopf). */
    public List<ItemStack> dropCurrentPage() {
        List<ItemStack> result = new ArrayList<>();
        int from = currentPage * SLOTS_PER_PAGE;
        int to = Math.min(storage.size(), from + SLOTS_PER_PAGE);
        for (int i = from; i < to; i++) {
            ItemStack stack = storage.getStack(i);
            if (!stack.isEmpty()) {
                result.add(stack.copy());
                storage.setStack(i, ItemStack.EMPTY);
            }
        }
        markDirty();
        return result;
    }

    /** Leert ALLE Seiten, fuer den "Alles verkaufen"-Knopf. Preisberechnung macht der Caller. */
    public List<ItemStack> clearAllStorage() {
        List<ItemStack> result = new ArrayList<>();
        for (int i = 0; i < storage.size(); i++) {
            ItemStack stack = storage.getStack(i);
            if (!stack.isEmpty()) {
                result.add(stack.copy());
                storage.setStack(i, ItemStack.EMPTY);
            }
        }
        markDirty();
        return result;
    }

    @Override
    public Text getDisplayName() {
        return Text.literal(data().displayName() + "-Spawner");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new net.hugospawner.screen.SpawnerScreenHandler(syncId, playerInventory, this);
    }

    public void openScreen(ServerPlayerEntity player) {
        player.openHandledScreen(this);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putInt("stackCount", stackCount);
        nbt.putInt("currentPage", currentPage);
        NbtCompound inv = new NbtCompound();
        Inventories.writeNbt(inv, storageAsList(), registries);
        nbt.put("storage", inv);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        stackCount = nbt.getInt("stackCount");
        currentPage = nbt.getInt("currentPage");
        resizeStorageIfNeeded();
        if (nbt.contains("storage")) {
            DefaultedListShim list = new DefaultedListShim(storage.size());
            Inventories.readNbt(nbt.getCompound("storage"), list.backing, registries);
            for (int i = 0; i < storage.size(); i++) {
                storage.setStack(i, list.backing.get(i));
            }
        }
    }

    private net.minecraft.util.collection.DefaultedList<ItemStack> storageAsList() {
        net.minecraft.util.collection.DefaultedList<ItemStack> list =
                net.minecraft.util.collection.DefaultedList.ofSize(storage.size(), ItemStack.EMPTY);
        for (int i = 0; i < storage.size(); i++) list.set(i, storage.getStack(i));
        return list;
    }

    /** Kleiner Helfer, weil DefaultedList kein direktes "from size" mit Lookup-Konstruktor hat. */
    private static final class DefaultedListShim {
        final net.minecraft.util.collection.DefaultedList<ItemStack> backing;
        DefaultedListShim(int size) {
            backing = net.minecraft.util.collection.DefaultedList.ofSize(size, ItemStack.EMPTY);
        }
    }
}
