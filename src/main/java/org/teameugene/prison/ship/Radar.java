package org.teameugene.prison.ship;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.teameugene.prison.Prison;
import org.teameugene.prison.Util.User;

import static org.teameugene.prison.Util.Utils.getUserFromPlayer;
import static org.teameugene.prison.items.ItemUtils.createItemGUI;

public class Radar {
    public static void openGUI(Player player) {
        Inventory upgradeGUI = Bukkit.createInventory(player, 54, "Radar");
        User user = getUserFromPlayer(player, Prison.connectedPlayers);
        assert user != null;
        if (user.getRadarActive()) {
            upgradeGUI.setItem(4 + 9, createItemGUI("§a§nActively Scanning", "", Material.GREEN_TERRACOTTA));
        } else {
            upgradeGUI.setItem(4 + 9, createItemGUI("§c§nDeactivated", "", Material.RED_TERRACOTTA));
        }

        upgradeGUI.setItem(53, createItemGUI("§6§oNext", "", Material.ARROW));
        player.openInventory(upgradeGUI);
    }

    public static void handleGUI(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() != null &&
                (event.getCurrentItem().getType().equals(Material.GREEN_TERRACOTTA)
                        || event.getCurrentItem().getType().equals(Material.RED_TERRACOTTA))) {
            User user = getUserFromPlayer(player, Prison.connectedPlayers);
            assert user != null;
            user.toggleRadarActive();
            openGUI(player);
        }
    }
}
