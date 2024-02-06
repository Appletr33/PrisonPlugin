package org.teameugene.prison.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.teameugene.prison.User;
import org.teameugene.prison.database.Database;

import java.util.ArrayList;

import static org.teameugene.prison.listeners.InventoryListener.openTeleportationGUI;
import static org.teameugene.prison.listeners.InventoryListener.openUpgradeGUI;
import static org.teameugene.prison.mine.Utils.getUserFromPlayer;
import static org.teameugene.prison.mine.Utils.getWorldByName;
import static org.teameugene.prison.scoreboard.ScoreBoard.displayScoreboard;

public class ItemListener implements Listener {

    Plugin plugin;
    Database database;
    String shipWorldName;
    ArrayList<User> connectedPlayers;

    public ItemListener(Plugin plugin, Database database, String shipWorldName, ArrayList<User> connectedPlayers) {
        this.plugin = plugin;
        this.database = database;
        this.shipWorldName = shipWorldName;
        this.connectedPlayers = connectedPlayers;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (isPick(player.getInventory().getItemInMainHand())) {
            if (player.isSneaking() && event.getAction() == Action.LEFT_CLICK_AIR)
                openUpgradeGUI(player, player.getInventory().getItemInMainHand());
        }

        else if (isTeleport(player.getInventory().getItemInMainHand())) {
            if (event.getAction() == Action.LEFT_CLICK_AIR) {
                openTeleportationGUI(player);
            }

            else if ((event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                // Cancel the event to prevent placing the redstone torch
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        //Prevent dropping our pickaxe or teleporter
        if (isTeleport(e.getItemDrop().getItemStack())) e.setCancelled(true);
        else if (isPick(e.getItemDrop().getItemStack())) e.setCancelled(true);
        else if (isSword(e.getItemDrop().getItemStack())) e.setCancelled(true);
    }

    static boolean isTeleport(ItemStack item) {
        // Check if the item has the desired display name and lore
        if (item != null && item.getType() == Material.REDSTONE_TORCH && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName() && meta.hasLore()) {
                return meta.getDisplayName().equals("Teleporter") && meta.getLore().contains("Teleports you instantaneously!");
            }
        }
        return false;
    }

    static boolean isSword(ItemStack item) {
        if (item != null && item.getType() == Material.NETHERITE_SWORD && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName() && meta.hasLore()) {
                return true;
            }
        }
        return false;
    }

    static boolean isPick(ItemStack item) {
        // Check if the item has the desired display name and lore
        if (item != null && item.getType() == Material.NETHERITE_PICKAXE && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName() && meta.hasLore()) {
                return meta.getDisplayName().equals("Cosmic Pickaxe") && meta.getLore().contains("Bestowed upon you by the heavens");
            }
        }
        return false;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block brokenBlock = event.getBlock();

        // Check if the broken block is stone
        if (brokenBlock.getType() == Material.STONE) {
            getUserFromPlayer(player, connectedPlayers).addPoints(1);
        }
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent e) {
        e.setCancelled(true);
    }

}
