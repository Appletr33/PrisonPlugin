package org.teameugene.prison.ship;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.teameugene.prison.Util.User;
import org.teameugene.prison.npcs.NPC;
import org.teameugene.prison.npcs.ShipMechanic;

import static org.teameugene.prison.items.ItemUtils.createItemGUI;

public class Ship {

    public static final String starterShipSchematicName = "ship2";

    public static void openShipUpgradeGUI(Player player) {
        Inventory upgradeGUI = Bukkit.createInventory(player, 9, "Ship Mechanic");

        upgradeGUI.setItem(3, createItemGUI("Cruiser", "Cost: " + ChatColor.GREEN + "10000", Material.COAL));
        upgradeGUI.setItem(4, createItemGUI("Flagship", "Cost: " + ChatColor.GREEN + "100000", Material.IRON_INGOT));
        upgradeGUI.setItem(5, createItemGUI("Mothership", "Cost: " + ChatColor.GREEN + "1000000", Material.DIAMOND));

        player.openInventory(upgradeGUI);
    }

    public static void handleShipGUIInteraction(Player player, String upgradeDisplayName, User user) {
        NPC shipMechanic = ShipMechanic.getInstance();
        shipMechanic.sendMessage(player, ChatColor.BLACK, "Ship upgrading not implemented yet my goodsir");
        player.closeInventory();
    }
}
