package org.teameugene.prison.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.teameugene.prison.User;
import org.teameugene.prison.database.Database;

import java.util.ArrayList;

import static org.teameugene.prison.Util.Utils.getUserFromPlayer;
import static org.teameugene.prison.items.ItemUtils.*;

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
            if ((player.isSneaking() && event.getAction() == Action.RIGHT_CLICK_AIR) || (player.isSneaking() && event.getAction() == Action.RIGHT_CLICK_BLOCK))
                openUpgradeGUI(player, player.getInventory().getItemInMainHand());
        }

        else if (isTeleport(player.getInventory().getItemInMainHand())) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR) {
                openTeleportationGUI(player);
            }

            else if ((event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                // Cancel the event to prevent placing the redstone torch
                event.setCancelled(true);
                openTeleportationGUI(player);
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



    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent e) {
        e.setCancelled(true);
    }

}
