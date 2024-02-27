package org.teameugene.prison;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.teameugene.prison.Util.*;
import org.teameugene.prison.commands.MapCommand;
import org.teameugene.prison.database.Database;
import org.teameugene.prison.listeners.*;
import org.teameugene.prison.mine.Mine;
import org.teameugene.prison.ship.Schematic;
import org.teameugene.prison.npcs.NPC;
import org.teameugene.prison.tasks.Tasks;
import org.teameugene.prison.worlds.EmptyVoidChunkGenerator;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static org.teameugene.prison.Util.Utils.*;

public final class Prison extends JavaPlugin implements Listener {

    private static Prison instance;
    public static Mine mine;
    public static Database database;
    public static final String shipWorldName = "shipworld";
    public static final String marsWorldName = "mars";
    public static final String moonWorldName = "moon";
    public static final String asteroidWorldName = "asteroid-world";
    public static boolean worldInitialization = false;
    public boolean reloaded = false;
    public static final ArrayList<User> connectedPlayers = new ArrayList<>();
    public static final Random random = new Random();

    @Override
    public void onEnable() {
        instance = this;
        // Pre-World Initialization
        getLogger().info("");
        getLogger().info("[START-UP]: (STARTED) Prison Initialization");
        getLogger().info("");
        //Register listener for world loading
        getServer().getPluginManager().registerEvents(this, this);
        //Init Database connection
        database = new Database(this);
        //Set random seed for random actions
        random.setSeed(System.currentTimeMillis() + 1349832);
        //Load Schematics
        Schematic.loadSchematics(this);
        //Init Namespaced Keys
        new Keys();
        try {
            CustomMaps.Initialize();
        } catch (FileNotFoundException | Execeptions.DirectoryNotFoundException e) {
            e.printStackTrace();
        }
        getLogger().info("[START-UP]: (Finished) Prison Initialization");
        if (reloaded) Initialize();
    }

    public void Initialize() {
        getLogger().info("[POST-WORLD]: (STARTED) Prison Initialization");
        //Reinitialize players on /reload
        for (Player player : Bukkit.getOnlinePlayers()) {
            initPlayer(database, player, connectedPlayers);
            PacketReader pr = new PacketReader(player);
            pr.inject();
        }

        //Initialize Text Entities
        TextEntities.initialize();
        //Create new mine
        mine = new Mine(this, new Location(getWorldByName(moonWorldName), -1729, 20, 768), new Location(getWorldByName(moonWorldName), -1776, 0, 815));
        //Spawn our NPCS
        NPC.setupNPCS();
        //Load tasks which depend on database connection being established
        new Tasks(this, database, connectedPlayers);
        //Load Objects
        Serialize.onLoad();
        //Initialize Game Objects post serialization
        new GameObjectManager();

        //Register Listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this, database, shipWorldName, connectedPlayers), this);
        getServer().getPluginManager().registerEvents(new ItemListener(this, database, shipWorldName, connectedPlayers), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this, database, connectedPlayers), this);
        getServer().getPluginManager().registerEvents(new NPCInteractListener(), this);
        getServer().getPluginManager().registerEvents(new MapListener(), this);
        getServer().getPluginManager().registerEvents(new ItemFrameListener(), this);

        //Register Commands
        Objects.requireNonNull(getCommand("map")).setExecutor(new MapCommand());

        getLogger().info("[POST-WORLD]: (FINISHED) Prison Initialization");
        getLogger().info("");
        getLogger().info("");
        getLogger().info("/////////////////////////////////////");
        getLogger().info("[COMPLETE]: Main Prison Plugin Active");
        getLogger().info("/////////////////////////////////////");
        getLogger().info("");
        getLogger().info("");
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (!worldInitialization) {
            worldInitialization = true; // this shit has to be first otherwise onWorldLoad gets called a ton in response
            setupWorlds();
            Initialize();
        }
    }

    public static Prison getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getScheduler().cancelTasks(this);
        // Close connection with database
        updateDatabase(database, connectedPlayers);
        database.closeConnection();
        //Save Objects
        Serialize.onSave();
    }

    private void setupWorlds() {
        List<String> worldNames = new ArrayList<>();
        worldNames.add(marsWorldName);
        worldNames.add(moonWorldName);
        worldNames.add(shipWorldName);
        worldNames.add(asteroidWorldName);

        for (String worldName : worldNames) {
            WorldCreator worldCreator = new WorldCreator(worldName);

            // Set the chunk generator for the world
            worldCreator.generator(new EmptyVoidChunkGenerator());
            Bukkit.createWorld(worldCreator);
        }

        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.KEEP_INVENTORY, true);
            world.setGameRule(GameRule.MOB_GRIEFING, false);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            world.setStorm(false);
        }

        getWorldByName(moonWorldName).setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        getWorldByName(moonWorldName).setTime(13000); // just before sunset

        getWorldByName(shipWorldName).setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        getWorldByName(shipWorldName).setTime(18000); // 12am

        getWorldByName(marsWorldName).setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        getWorldByName(marsWorldName).setTime(9000); // 12pm

        getWorldByName(asteroidWorldName).setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        getWorldByName(asteroidWorldName).setTime(18000); //12am

    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new EmptyVoidChunkGenerator();
    }
}
