package net.hugospawner.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

/** FILL: eigene Textur fuer den Shop, siehe Hinweis in SpawnerScreen. */
public class SpawnerShopScreen extends HandledScreen<SpawnerShopScreenHandler> {

    public SpawnerShopScreen(SpawnerShopScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, Text.literal("Spawner Shop"));
        this.backgroundWidth = 176;
        this.backgroundHeight = 18 + 3 * 18 + 14 + 3 * 18 + 58 + 14;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.fill(x, y, x + backgroundWidth, y + backgroundHeight, 0xC0101010);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(textRenderer, Text.literal("Spawner Shop"), titleX, titleY, 0xFFFFFF, false);
    }
}
