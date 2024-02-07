package org.teameugene.prison;

import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.teameugene.prison.database.Database;
import org.teameugene.prison.listeners.InventoryListener;
import org.teameugene.prison.listeners.ItemListener;
import org.teameugene.prison.listeners.PlayerListener;
import org.teameugene.prison.mine.Mine;
import org.teameugene.prison.mine.Schematic;
import org.teameugene.prison.tasks.Tasks;
import org.teameugene.prison.worlds.mars.Mars;

import java.util.ArrayList;

import static org.teameugene.prison.Util.Utils.getWorldByName;
import static org.teameugene.prison.Util.Utils.updateDatabase;

public final class Prison extends JavaPlugin {

    private static Prison instance;
    Mine mine;
    Database database;
    ArrayList<Schematic> schematics;
    String shipWorldName = "shipworld";
    String marsWorldName = "mars";
    ArrayList<User> connectedPlayers;

    public static final Location corner1 = new Location(getWorldByName("world"), -1729, 20, 768);
    public static final Location corner2 = new Location(getWorldByName("world"), -1776, 0, 815);

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("[STARTING]: Initializing Main Prison Plugin");

        //Plugin Initialization Logic

        //Initialize activePlayers ArrayList
        connectedPlayers = new ArrayList<>();

        //Set Keep Inventory and time rules
        setRules();

        //Load Schematics
        schematics = Schematic.loadSchematics(this);

        //Create new mine
        mine = new Mine(this, corner1, corner2);
        //Connect to database
        database = new Database(this);

        //Load tasks which depend on database connection being established
        new Tasks(this, database, connectedPlayers);

        //Register Other Planets
        Mars mars = new Mars(this);

        //Register Listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this, database, schematics, shipWorldName, connectedPlayers), this);
        getServer().getPluginManager().registerEvents(new ItemListener(this, database, shipWorldName, connectedPlayers), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this, database, connectedPlayers), this);

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

        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.KEEP_INVENTORY, true);
            world.setGameRule(GameRule.MOB_GRIEFING, false);
        }
        getWorldByName(shipWorldName).setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        getWorldByName(shipWorldName).setTime(18000); // 12am

        getWorldByName(marsWorldName).setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        getWorldByName(marsWorldName).setTime(9000); // 12am
    }
}
