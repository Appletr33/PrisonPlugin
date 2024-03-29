package org.teameugene.prison.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.teameugene.prison.Prison;
import org.teameugene.prison.Util.PacketReader;
import org.teameugene.prison.Util.User;
import org.teameugene.prison.Util.Utils;
import org.teameugene.prison.database.Database;
import org.teameugene.prison.ship.Schematic;
import org.teameugene.prison.ship.Ship;

import java.util.ArrayList;
import java.util.UUID;

import static org.teameugene.prison.Util.Utils.*;
import static org.teameugene.prison.enums.Ore.oreToItem;
import static org.teameugene.prison.items.Upgrades.*;
import static org.teameugene.prison.npcs.NPC.showNPCS;

public class PlayerListener implements org.bukkit.event.Listener {

    Database database;
    Plugin plugin;
    ArrayList<User> connectedPlayers;
    String shipWorldName;

    public PlayerListener(Plugin plugin, Database database, String shipWorldName, ArrayList<User> players) {
        this.database = database;
        this.plugin = plugin;
        this.shipWorldName = shipWorldName;
        this.connectedPlayers = players;
    }

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUniqueId = player.getUniqueId();
        String sPlayerUniqueId = playerUniqueId.toString();

        /*
                        NEW PLAYER LOGIC
         */
        // Check if the player is in the database
        if (!database.isPlayerInDatabase(sPlayerUniqueId)) {
            // If not, create an entry for them
            database.createPlayerEntry(sPlayerUniqueId);
            //player is a new player so set them up
            newPlayer(player, Ship.starterShipSchematicName, shipWorldName, connectedPlayers, database);
        }
         /*
                        END NEW PLAYER LOGIC
         */

        initPlayer(database, player, connectedPlayers);
        showNPCS(event.getPlayer());
        PacketReader pr = new PacketReader(event.getPlayer());
        pr.inject();
        applyContinuousArmorUpgrades(player);
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        showNPCS(event.getPlayer());

        //Remove any bounds restrictions from the player
        User user = Utils.getUserFromPlayer(event.getPlayer(), connectedPlayers);
        if (user != null)
            user.unRestrict();

        //Turn on or off ability to use jetpack based on world
        if(event.getPlayer().getWorld() == Utils.getWorldByName(Prison.asteroidWorldName)) {
            Jetpack.enableGravity(event.getPlayer());
        } else {
            Jetpack.disableGravity(event.getPlayer());
        }

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        Location playerLocation = player.getLocation();

        //Remove User and update points on leave
        for (User user : connectedPlayers) {
            if (user.getUUID().equals(playerUUID))
                database.updatePoints(user.getUUID(), user.getPoints());
        }
        connectedPlayers.removeIf(user -> user.getUUID().equals(playerUUID));

        //Set players pos back to spawn
        player.teleport(getWorldByName(Prison.moonWorldName).getSpawnLocation());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.getDrops().clear();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true); // Cancel fall damage for players
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        // Cancel food level change events
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block brokenBlock = event.getBlock();
        event.setDropItems(false);

        if (brokenBlock.getType() != Material.STONE) {
            if (isInRegion(brokenBlock.getLocation(), Prison.mine.corner1, Prison.mine.corner2) || player.getWorld().equals(Utils.getWorldByName(Prison.asteroidWorldName))) {
                ItemStack[] drops = brokenBlock.getDrops().toArray(new ItemStack[0]);
                for (ItemStack drop : drops) {
                    drop = oreToItem(drop);
                    addItemOrDrop(player, drop);
                }
            }
        }

        getUserFromPlayer(player, connectedPlayers).addPoints(1);
        ItemStack itemUsed = player.getInventory().getItemInMainHand();
        applyMiningUpgrades(connectedPlayers, player, itemUsed, brokenBlock);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity attackerEntity = event.getDamager();
        if (attackerEntity instanceof Player) {
            Player attacker = (Player) attackerEntity;
            applySwordUpgrades(attacker, attacker.getInventory().getItemInMainHand());
            Entity targetEntity = event.getEntity();
            if (targetEntity instanceof Player) {
            } else {
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        User user = Utils.getUserFromPlayer(event.getPlayer(), connectedPlayers);
        assert user != null;
        if (!user.isRestricted())
            return;

        Location to = event.getTo();
        Location from = event.getFrom();

        double deltaX = to.getX() - user.getRestrictionOrigin().getX();
        double deltaZ = to.getZ() - user.getRestrictionOrigin().getZ();

        double distanceSquared = deltaX * deltaX + deltaZ * deltaZ;

        // Check if the player has moved beyond the maximum distance from the center location
        if (distanceSquared > user.getRestrictionRadius() * user.getRestrictionRadius()) {
            // Player has moved beyond the allowed distance, teleport them back
            event.getPlayer().teleport(from);
        }
    }
}