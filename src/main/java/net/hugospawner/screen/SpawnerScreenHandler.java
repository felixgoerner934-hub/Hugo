package net.hugospawner.screen;

import net.hugospawner.block.SpawnerBlockEntity;
import net.hugospawner.registry.ModScreenHandlers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

/**
 * Zeigt eine Seite (36 Slots) des Spawner-Lagers + normales Spieler-Inventar.
 * Server-seitig zeigt storageView direkt auf die echte Lager-Inventory der
 * SpawnerBlockEntity (ueber PagedInventoryView, kein Kopieren).
 */
public class SpawnerScreenHandler extends ScreenHandler {

    private static final int STORAGE_ROWS = 4; // 36 Slots = 4x9
    private static final int STORAGE_COLS = 9;
    private static final int STORAGE_SLOTS = STORAGE_ROWS * STORAGE_COLS;

    private final SpawnerBlockEntity blockEntity; // null auf dem Client
    private final Inventory storageView;
    private final Inventory buttonBar = new SimpleInventory(4); // nur fuers Rendering/Icons, keine echten Items

    /** Client-Konstruktor, von HandledScreens.register ueber Reflection genutzt. */
    public SpawnerScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(ModScreenHandlers.SPAWNER_SCREEN_HANDLER, syncId);
        this.blockEntity = null;
        this.storageView = new SimpleInventory(STORAGE_SLOTS);
        addSlots(playerInventory);
    }

    /** Server-Konstruktor: echte Lager-Inventory der BlockEntity, aktuelle Seite. */
    public SpawnerScreenHandler(int syncId, PlayerInventory playerInventory, SpawnerBlockEntity blockEntity) {
        super(ModScreenHandlers.SPAWNER_SCREEN_HANDLER, syncId);
        this.blockEntity = blockEntity;
        int offset = blockEntity.getCurrentPage() * STORAGE_SLOTS;
        this.storageView = new PagedInventoryView(blockEntity.pageInventory(), offset, STORAGE_SLOTS);
        setButtonIcons();
        addSlots(playerInventory);
    }

    // FILL: bis eigene Texturen fuer Sell/Prev/Next/Drop existieren, dienen diese
    // Vanilla-Items nur als Platzhalter-Icons in der Button-Leiste.
    private void setButtonIcons() {
        buttonBar.setStack(BUTTON_SELL_ALL - STORAGE_SLOTS, new ItemStack(Items.EMERALD));
        buttonBar.setStack(BUTTON_PREV_PAGE - STORAGE_SLOTS, new ItemStack(Items.ARROW));
        buttonBar.setStack(BUTTON_NEXT_PAGE - STORAGE_SLOTS, new ItemStack(Items.SPECTRAL_ARROW));
        buttonBar.setStack(BUTTON_DROP_PAGE - STORAGE_SLOTS, new ItemStack(Items.HOPPER));
    }

    private void addSlots(PlayerInventory playerInventory) {
        for (int row = 0; row < STORAGE_ROWS; row++) {
            for (int col = 0; col < STORAGE_COLS; col++) {
                this.addSlot(new Slot(storageView, col + row * STORAGE_COLS, 8 + col * 18, 18 + row * 18));
            }
        }

        int buttonY = 18 + STORAGE_ROWS * 18 + 4;
        int[] buttonX = {8, 26, 44, 62}; // Sell / Prev / Next / Drop, nebeneinander
        for (int i = 0; i < 4; i++) {
            this.addSlot(new NonInteractiveSlot(buttonBar, i, buttonX[i], buttonY));
        }

        int playerInvY = buttonY + 24;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, playerInvY + 58));
        }
    }

    public SpawnerBlockEntity getBlockEntity() {
        return blockEntity;
    }

    /**
     * Button-Leiste als 4 nicht-entnehmbare Slots: SELL_ALL(36), PREV_PAGE(37),
     * NEXT_PAGE(38), DROP_PAGE(39) - Indizes direkt hinter dem Lager, noch vor dem
     * Spieler-Inventar. Icons setzt der Client-Screen rein kosmetisch im Rendering,
     * die Slots selbst bleiben in der Lager-Inventory leer.
     */
    public static final int BUTTON_SELL_ALL = STORAGE_SLOTS;
    public static final int BUTTON_PREV_PAGE = STORAGE_SLOTS + 1;
    public static final int BUTTON_NEXT_PAGE = STORAGE_SLOTS + 2;
    public static final int BUTTON_DROP_PAGE = STORAGE_SLOTS + 3;

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (blockEntity != null && slotIndex >= BUTTON_SELL_ALL && slotIndex <= BUTTON_DROP_PAGE) {
            handleButton(slotIndex, player);
            return; // kein super-Aufruf: verhindert normales Item-Pickup auf den Button-Slots
        }
        super.onSlotClick(slotIndex, button, actionType, player);
    }

    private void handleButton(int slotIndex, PlayerEntity player) {
        if (slotIndex == BUTTON_SELL_ALL) {
            // Preisberechnung/Gem-Gutschrift macht GemManager, s. economy-Paket.
            for (ItemStack stack : blockEntity.clearAllStorage()) {
                net.hugospawner.economy.GemManager.sellDrops(player, blockEntity, stack);
            }
        } else if (slotIndex == BUTTON_PREV_PAGE) {
            blockEntity.setPage(blockEntity.getCurrentPage() - 1);
            reopenForPageChange(player);
        } else if (slotIndex == BUTTON_NEXT_PAGE) {
            blockEntity.setPage(blockEntity.getCurrentPage() + 1);
            reopenForPageChange(player);
        } else if (slotIndex == BUTTON_DROP_PAGE) {
            for (ItemStack stack : blockEntity.dropCurrentPage()) {
                if (!player.getInventory().insertStack(stack)) {
                    player.dropItem(stack, false);
                }
            }
        }
    }

    private void reopenForPageChange(PlayerEntity player) {
        if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
            blockEntity.openScreen(serverPlayer);
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasStack()) return ItemStack.EMPTY;

        ItemStack original = slot.getStack();
        ItemStack copy = original.copy();

        if (index < STORAGE_SLOTS) {
            if (!this.insertItem(original, STORAGE_SLOTS, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.insertItem(original, 0, STORAGE_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (original.isEmpty()) slot.setStack(ItemStack.EMPTY);
        else slot.markDirty();
        return copy;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return blockEntity == null || blockEntity.pageInventory().canPlayerUse(player);
    }

    /** Slot, der nur als Icon-Anzeige dient - Items koennen weder rein noch raus. */
    private static final class NonInteractiveSlot extends Slot {
        NonInteractiveSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        @Override
        public boolean canTakeItems(PlayerEntity player) {
            return false;
        }
    }
}
