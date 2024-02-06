package org.teameugene.prison.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.teameugene.prison.User;
import org.teameugene.prison.database.Database;
import org.teameugene.prison.mine.Schematic;
import org.teameugene.prison.mine.Utils;

import java.util.ArrayList;
import java.util.UUID;

import static org.teameugene.prison.mine.Utils.getWorldByName;
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
            newPlayer(player);
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

    private void newPlayer(Player player) {
        player.sendMessage("Welcome to the moon trooper, " + player.getName() + "!");
        Utils.giveItemsToPlayer(player);
        createStarterShip(player);
    }

    private void createStarterShip(Player player) {
        double[] pos = database.getPlayerShipCoordinates(player.getUniqueId());
        for (Schematic schematic : schematicArrayList) {
            if (schematic.getName().equals(starterShipSchematicName + ".schem")) {
                schematic.paste(new Location(getWorldByName(shipWorldName), pos[0] - 14, pos[1] - 5, pos[2])); //-14 and -5 are offset values for the schematic so the player spawns on the ship
            }
        }
    }
}