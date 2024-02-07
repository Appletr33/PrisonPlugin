package org.teameugene.prison.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.teameugene.prison.User;
import org.teameugene.prison.database.Database;
import org.teameugene.prison.enums.Upgrade;
import org.teameugene.prison.mine.Schematic;
import org.teameugene.prison.Util.Utils;

import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.UUID;

import static org.teameugene.prison.Util.Utils.*;
import static org.teameugene.prison.items.ItemUtils.getItemUpgrades;
import static org.teameugene.prison.items.ItemUtils.getLevel;
import static org.teameugene.prison.items.Upgrades.detonateBlocks;
import static org.teameugene.prison.items.Upgrades.speedUpgrade;
import static org.teameugene.prison.scoreboard.ScoreBoard.displayScoreboard;

public class PlayerListener implements org.bukkit.event.Listener {

    Database database;
    Plugin plugin;
    ArrayList<Schematic> schematicArrayList;
    ArrayList<User> connectedPlayers;
    String starterShipSchematicName = "spaceship-1";
    String shipWorldName;

    public PlayerListener(Plugin plugin, Database database, ArrayList<Schematic> schematicArrayList, String shipWorldName, ArrayList<User> players) {
        this.database = database;
        this.plugin = plugin;
        this.schematicArrayList = schematicArrayList;
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
            newPlayer(player, schematicArrayList, starterShipSchematicName, shipWorldName, connectedPlayers, database);
        }
         /*
                        END NEW PLAYER LOGIC
         */
        User user = new User(database.getPoints(playerUniqueId), player);
        this.connectedPlayers.add(user);
        displayScoreboard(player, user.getPoints());
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
        player.teleport(getWorldByName("world").getSpawnLocation());
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

        if (brokenBlock.getType() == Material.STONE)
            event.setDropItems(false);

        getUserFromPlayer(player, connectedPlayers).addPoints(1);

        ItemStack itemUsed = player.getInventory().getItemInMainHand();
        ArrayList<Upgrade> itemUpgrades = getItemUpgrades(itemUsed);

        for (Upgrade upgrade : itemUpgrades) {
            if (upgrade.equals(Upgrade.ATOMIC_DETONATE)) {
                if (brokenBlock.getType() == Material.STONE) {
                    int level = getLevel(upgrade, itemUsed);
                    getUserFromPlayer(player, connectedPlayers).addPoints(detonateBlocks(brokenBlock, level, player, connectedPlayers));

                }
            }
            if (upgrade.equals(Upgrade.SPEED)) {
                int level = getLevel(upgrade, itemUsed);
                speedUpgrade(player, level);
            }
        }
    }
}