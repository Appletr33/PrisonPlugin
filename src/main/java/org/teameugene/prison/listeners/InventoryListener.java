package org.teameugene.prison.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.teameugene.prison.User;
import org.teameugene.prison.database.Database;
import org.teameugene.prison.enums.Upgrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.teameugene.prison.Util.RomanParser.intToRoman;
import static org.teameugene.prison.Util.RomanParser.romanToInt;
import static org.teameugene.prison.Util.Utils.*;
import static org.teameugene.prison.items.ItemUtils.handleTeleportationAction;
import static org.teameugene.prison.items.ItemUtils.handleUpgrade;

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

        // PREVENT ARMOR FROM BEING REMOVED
        if(event.getSlotType() == InventoryType.SlotType.ARMOR) {
            event.setCancelled(true);
        }
    }
}
