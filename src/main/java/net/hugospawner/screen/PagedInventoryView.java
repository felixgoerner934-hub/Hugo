package net.hugospawner.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

/**
 * Durchreichender Blick auf einen Ausschnitt (eine "Seite") einer groesseren
 * Inventory, OHNE die Items zu kopieren. Schreibzugriffe landen direkt in der
 * echten SpawnerBlockEntity-Lager-Inventory.
 */
public class PagedInventoryView implements Inventory {

    private final Inventory backing;
    private final int offset;
    private final int size;

    public PagedInventoryView(Inventory backing, int offset, int size) {
        this.backing = backing;
        this.offset = offset;
        this.size = size;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < size; i++) {
            if (!backing.getStack(offset + i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return backing.getStack(offset + slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return backing.removeStack(offset + slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return backing.removeStack(offset + slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        backing.setStack(offset + slot, stack);
    }

    @Override
    public void markDirty() {
        backing.markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return backing.canPlayerUse(player);
    }
}
