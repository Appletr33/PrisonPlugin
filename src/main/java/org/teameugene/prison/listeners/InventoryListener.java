package org.teameugene.prison.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.Plugin;
import org.teameugene.prison.Util.GameObjectManager;
import org.teameugene.prison.Util.User;
import org.teameugene.prison.database.Database;

import java.util.ArrayList;

import static org.teameugene.prison.Util.Utils.*;
import static org.teameugene.prison.items.ItemUtils.handleTeleportationAction;
import static org.teameugene.prison.items.ItemUtils.handleUpgrade;
import static org.teameugene.prison.ship.Ship.*;

public class InventoryListener implements Listener {

    Plugin plugin;
    Database database;
    ArrayList<User> connectedPlayers;

    public InventoryListener(Plugin plugin, Database database, ArrayList<User> connectedPlayers) {
        this.plugin = plugin;
        this.database = database;
        this.connectedPlayers = connectedPlayers;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().contains("Upgrade")) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null &&
                    event.getCurrentItem().getItemMeta() != null &&
                    event.getCurrentItem().getItemMeta().hasLore() &&
                    event.getCurrentItem().getItemMeta().getLore().get(0).startsWith("Cost:")) {

                // Handle the upgrade logic here
                handleUpgrade((Player) event.getWhoClicked(), event.getCurrentItem().getItemMeta().getDisplayName(), connectedPlayers);
            }
        }
        else if (event.getView().getTitle().contains("Mechanic")) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null &&
                    event.getCurrentItem().getItemMeta() != null &&
                    event.getCurrentItem().getItemMeta().hasLore() &&
                    event.getCurrentItem().getItemMeta().getLore().get(0).startsWith("Cost:")) {

                Player player = (Player) event.getWhoClicked();
                handleShipGUIInteraction(player, event.getCurrentItem().getItemMeta().getDisplayName(), getUserFromPlayer(player, connectedPlayers));
            }
        }
        else if (event.getView().getTitle().equals("Warps")) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null &&
                    (event.getCurrentItem().getType() == Material.END_STONE || event.getCurrentItem().getType() == Material.ELYTRA || event.getCurrentItem().getType() == Material.RED_TERRACOTTA) &&
                    event.getCurrentItem().getItemMeta() != null &&
                    event.getCurrentItem().getItemMeta().hasLore() &&
                    event.getCurrentItem().getItemMeta().getLore().get(0).contains("Teleports you to")) {

                // Handle the upgrade logic here
                handleTeleportationAction((Player) event.getWhoClicked(), event.getCurrentItem().getItemMeta().getDisplayName(), database);
            }
        }
        else if (event.getView().getTitle().equals("Radar")) {
            event.setCancelled(true);
            GameObjectManager.radars.get(event.getWhoClicked().getUniqueId().toString()).handleGUI(event);
        }

        // PREVENT ARMOR FROM BEING REMOVED
        if(event.getSlotType() == InventoryType.SlotType.ARMOR) {
            event.setCancelled(true);
        }
    }
}
