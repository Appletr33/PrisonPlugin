package org.teameugene.prison;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.teameugene.prison.Util.PacketReader;
import org.teameugene.prison.Util.TextEntities;
import org.teameugene.prison.Util.User;
import org.teameugene.prison.database.Database;
import org.teameugene.prison.listeners.*;
import org.teameugene.prison.mine.Mine;
import org.teameugene.prison.ship.Schematic;
import org.teameugene.prison.npcs.NPC;
import org.teameugene.prison.tasks.Tasks;
import org.teameugene.prison.worlds.EmptyVoidChunkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.teameugene.prison.Util.Utils.*;

public final class Prison extends JavaPlugin implements Listener {

    private static Prison instance;
    public static Mine mine;
    public static Database database;
    ArrayList<Schematic> schematics;
    public static final String shipWorldName = "shipworld";
    public static final String marsWorldName = "mars";
    public static final String moonWorldName = "moon";
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
        schematics = Schematic.loadSchematics(this);
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

        //Register Listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this, database, schematics, shipWorldName, connectedPlayers), this);
        getServer().getPluginManager().registerEvents(new ItemListener(this, database, shipWorldName, connectedPlayers), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this, database, connectedPlayers), this);
        getServer().getPluginManager().registerEvents(new NPCInteractListener(), this);

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
        updateDatabase(database, connectedPlayers);
        database.closeConnection();
    }

    private void setupWorlds() {
        List<String> worldNames = new ArrayList<>();
        worldNames.add(marsWorldName);
        worldNames.add(moonWorldName);
        worldNames.add(shipWorldName);

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
        getWorldByName(marsWorldName).setTime(9000); // 12am
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new EmptyVoidChunkGenerator();
    }
}
