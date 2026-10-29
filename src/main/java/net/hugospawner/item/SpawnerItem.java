package net.hugospawner.item;

import net.hugospawner.block.SpawnerBlock;
import net.hugospawner.spawner.SpawnerData;
import net.hugospawner.spawner.SpawnerDataLoader;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Das Item, das der Spieler im Inventar haelt/kauft. Platziert SpawnerBlock und
 * traegt den Mob-Typ (typeId), damit Block/Item/BlockEntity konsistent zusammenpassen.
 */
public class SpawnerItem extends BlockItem {

    private final String typeId;

    public SpawnerItem(String typeId, SpawnerBlock block, Item.Settings settings) {
        super(block, settings);
        this.typeId = typeId;
    }

    public String getTypeId() {
        return typeId;
    }

    @Override
    public Text getName(ItemStack stack) {
        return Text.literal(SpawnerDataLoader.get(typeId).displayName() + "-Spawner");
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        SpawnerData d = SpawnerDataLoader.get(typeId);
        tooltip.add(Text.literal("§7Loot ohne echte Mobs"));
        tooltip.add(Text.literal("§7Rechtsklick öffnet das Menü"));
        tooltip.add(Text.literal("§7Stapeln: mehr Loot, mehr Lager"));
        if (!d.confirmed()) {
            tooltip.add(Text.literal("§8(Platzhalter-Werte, noch nicht aus HugoSMP bestätigt)"));
        }
    }

    // ACHTUNG: appendTooltip hiess in juengeren 1.21.x-Versionen zeitweise
    // appendTooltip(ItemStack, Item.TooltipContext, TooltipType, Consumer<Text>) o.ae. -
    // beim ersten Build gegen die tatsaechliche Item-Klasse in den 1.21.11-Yarn-Sources
    // pruefen (Strg+Klick auf appendTooltip in IntelliJ zeigt die echte Signatur).
}
