package net.hugospawner.screen;

import net.hugospawner.economy.GemManager;
import net.hugospawner.registry.ModItems;
import net.hugospawner.registry.ModScreenHandlers;
import net.hugospawner.spawner.SpawnerData;
import net.hugospawner.spawner.SpawnerDataLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

/**
 * Alte In-Game-"Spawner Shop"-GUI (Chest-Layout, 3 Reihen a 9 Slots wie im
 * Referenz-Screenshot). Reihe 2 zeigt die 7 Spawner-Items, Klick = Kaufversuch
 * fuer 1 Stueck. Beschriftung/Lore frei gewaehlt, Preis kommt aus spawner_types.json.
 */
public class SpawnerShopScreenHandler extends ScreenHandler {

    private static final int ROWS = 3;
    private static final int COLS = 9;
    private static final int SHOP_SLOTS = ROWS * COLS;

    private final Inventory shopDisplay = new SimpleInventory(SHOP_SLOTS);

    public SpawnerShopScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(ModScreenHandlers.SPAWNER_SHOP_SCREEN_HANDLER, syncId);
        fillDisplay();

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                this.addSlot(new ShopSlot(shopDisplay, col + row * COLS, 8 + col * 18, 18 + row * 18));
            }
        }

        int playerInvY = 18 + ROWS * 18 + 14;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, playerInvY + 58));
        }
    }

    private void fillDisplay() {
        int slot = 10; // zweite Reihe, ab 2. Spalte - wie im Referenzscreenshot
        for (String typeId : ModItems.TYPE_IDS) {
            SpawnerData data = SpawnerDataLoader.get(typeId);
            ItemStack display = new ItemStack(ModItems.get(typeId));
            display.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME,
                    Text.literal("§d" + data.displayName() + "-Spawner §7(" + data.priceGems() + " Gems)"));
            shopDisplay.setStack(slot, display);
            slot++;
        }
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex >= 0 && slotIndex < SHOP_SLOTS) {
            ItemStack clicked = shopDisplay.getStack(slotIndex);
            if (!clicked.isEmpty()) {
                tryPurchase(player, clicked);
            }
            return; // nie normales Entnehmen erlauben
        }
        super.onSlotClick(slotIndex, button, actionType, player);
    }

    private void tryPurchase(PlayerEntity player, ItemStack displayStack) {
        if (!(displayStack.getItem() instanceof net.hugospawner.item.SpawnerItem spawnerItem)) return;
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return;

        SpawnerData data = SpawnerDataLoader.get(spawnerItem.getTypeId());
        boolean charged = GemManager.tryCharge(serverWorld, player.getUuid(), data.priceGems());
        if (!charged) {
            player.sendMessage(Text.literal("§cEs fehlen Gems (" + data.priceGems() + " Gems benoetigt)"), true);
            return;
        }

        ItemStack toGive = new ItemStack(spawnerItem, 1);
        if (!player.getInventory().insertStack(toGive)) {
            player.dropItem(toGive, false);
        }
        player.sendMessage(Text.literal("§aGekauft: " + data.displayName() + "-Spawner"), true);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        return ItemStack.EMPTY; // Shop-Fenster hat keinen Item-Transfer, nur Kauf per Klick
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    private static final class ShopSlot extends Slot {
        ShopSlot(Inventory inventory, int index, int x, int y) {
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
