package org.teameugene.prison.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.plugin.Plugin;
import org.teameugene.prison.Util.User;
import org.teameugene.prison.database.Database;

import java.util.ArrayList;

import static org.teameugene.prison.enums.CustomItem.*;

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
       if (isCustomItem(player.getInventory().getItemInMainHand())) {
           handleToolLogic(event);
       }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (isCustomItem(e.getItemDrop().getItemStack())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent e) {
        e.setCancelled(true);
    }

}
