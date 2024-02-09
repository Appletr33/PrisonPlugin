package org.teameugene.prison;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.teameugene.prison.Util.TextEntities;
import org.teameugene.prison.database.Database;
import org.teameugene.prison.listeners.InventoryListener;
import org.teameugene.prison.listeners.ItemListener;
import org.teameugene.prison.listeners.NPCInteractListener;
import org.teameugene.prison.listeners.PlayerListener;
import org.teameugene.prison.mine.Mine;
import org.teameugene.prison.ship.Schematic;
import org.teameugene.prison.npcs.NPC;
import org.teameugene.prison.tasks.Tasks;
import org.teameugene.prison.worlds.mars.Mars;

import java.util.ArrayList;
import java.util.Random;

import static org.teameugene.prison.Util.Utils.*;

public final class Prison extends JavaPlugin {

    private static Prison instance;
    Mine mine;
    public static Database database;
    ArrayList<Schematic> schematics;
    public static final String shipWorldName = "shipworld";
    public static final String marsWorldName = "mars";
    public static final String moonWorldName = "world";
    public static final ArrayList<User> connectedPlayers = new ArrayList<>();
    public static final Random random = new Random();

    public static final Location corner1 = new Location(getWorldByName("world"), -1729, 20, 768);
    public static final Location corner2 = new Location(getWorldByName("world"), -1776, 0, 815);

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("[STARTING]: Initializing Main Prison Plugin");

        //Plugin Initialization Logic

        //Connect to database
        database = new Database(this);

        //Set Random Seed
        random.setSeed(System.currentTimeMillis() + 1349832);

        //Initialize activePlayers ArrayList
        for (Player player : Bukkit.getOnlinePlayers()) {
            initPlayer(database, player, connectedPlayers);
        }

        //Set Keep Inventory and time rules
        setRules();

        //Initialize Text Entities
        TextEntities.initialize();

        //Load Schematics
        schematics = Schematic.loadSchematics(this);

        //Create new mine
        mine = new Mine(this, corner1, corner2);

        //Load tasks which depend on database connection being established
        new Tasks(this, database, connectedPlayers);

        //Register Other Planets
        Mars mars = new Mars(this);

        //Register Listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this, database, schematics, shipWorldName, connectedPlayers), this);
        getServer().getPluginManager().registerEvents(new ItemListener(this, database, shipWorldName, connectedPlayers), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this, database, connectedPlayers), this);
        getServer().getPluginManager().registerEvents(new NPCInteractListener(), this);

        //Spawn our NPCS
        NPC.setupNPCS();

        getLogger().info("[COMPLETED]: Finished Initializing Main Prison Plugin");
    }

    public static Prison getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        updateDatabase(database, connectedPlayers);
        database.closeConnection();
    }

    private void setRules() {
        if (Bukkit.getWorld(shipWorldName) == null){
            new WorldCreator(shipWorldName).createWorld();
        }

        if (Bukkit.getWorld(marsWorldName) == null){
            new WorldCreator(marsWorldName).createWorld();
        }

        if (Bukkit.getWorld(moonWorldName) == null) {
            new WorldCreator(moonWorldName).createWorld();
        }

        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.KEEP_INVENTORY, true);
            world.setGameRule(GameRule.MOB_GRIEFING, false);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            world.setStorm(false);
        }
        getWorldByName("world").setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        getWorldByName("world").setTime(13000); // just before sunset

        getWorldByName(shipWorldName).setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        getWorldByName(shipWorldName).setTime(18000); // 12am

        getWorldByName(marsWorldName).setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        getWorldByName(marsWorldName).setTime(9000); // 12am
    }
}
