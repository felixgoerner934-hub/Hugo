package net.hugospawner.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * FILL: textures/gui/spawner.png existiert noch nicht. Bis dahin wird ein
 * schlichtes Vanilla-Panel ohne eigenes Artwork gezeichnet, damit die GUI
 * trotzdem benutzbar ist. Ersetze TEXTURE sobald du ein Textur-Asset hast
 * (Referenzdesign: lila "SPAWNER"-Kopfzeile, siehe deine Screenshots).
 */
public class SpawnerScreen extends HandledScreen<SpawnerScreenHandler> {

    private static final Identifier TEXTURE = Identifier.of("hugospawner", "textures/gui/spawner.png");

    public SpawnerScreen(SpawnerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, Text.literal("Spawner"));
        this.backgroundWidth = 176;
        this.backgroundHeight = 18 + 4 * 18 + 24 + 14 + 3 * 18 + 58 + 14;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        // FILL: context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight)
        // sobald die Textur existiert. Bis dahin reicht der generische Rahmen aus,
        // den HandledScreen ueber drawSlot automatisch mitzeichnet.
        context.fill(x, y, x + backgroundWidth, y + backgroundHeight, 0xC0101010);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(textRenderer, Text.literal("SPAWNER"), titleX, titleY, 0xFFFFFF, false);
    }
}
