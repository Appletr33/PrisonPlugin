package org.teameugene.prison.mine;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.teameugene.prison.Prison;

import java.util.Random;

import static org.teameugene.prison.Util.Utils.*;

public class Mine {

    private long regenationTime = 1;
    private int warnCount = 0;
    private final int maxWarnCount = 2;
    Plugin plugin;
    World world;

    public Location corner1;
    public Location corner2;

    public Mine(Plugin plugin, Location corner1, Location corner2) {
        this.plugin = plugin;
        world = Bukkit.getWorld(Prison.moonWorldName);
        this.corner1 = corner1;
        this.corner2 = corner2;


        registerTasks();
    }

    private void registerTasks() {
        //Register Regeneration Task
        new BukkitRunnable() {
            @Override
            public void run() {
                //1 min warning
                broadcastMessageInWorld(getWorldByName(Prison.moonWorldName), "§6New ore will rise from the mine in 60 seconds!");

                //Warning Task
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (warnCount < maxWarnCount) {
                            // Call the function you want to execute
                            broadcastWarning();

                            // Increment the task count
                            warnCount++;
                        } else {
                            // Cancel the task after it has run 10 times
                            warnCount = 0;
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 20 * 50, 20 * 5);

                //Regen Task
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        regenerateMine();
                    }
                }.runTaskLater(plugin, 20L * 60 * 1); //1 min delay
            }
        }.runTaskTimer(plugin, 0, 20 * 60 * regenationTime); // 20 minutes in ticks
    }

    private void broadcastWarning() {
        broadcastMessageInWorld(getWorldByName(Prison.moonWorldName),"New Ore Arriving in §4" + (maxWarnCount - warnCount) + "§fs!");
    }

    private void regenerateMine() {
        //plugin.getLogger().info("[INFO]: Regenerating Mine");
        checkPlayerLocations();
        regenerateBlocks();
        //plugin.getLogger().info("[INFO]: Mine Successfully Regenerated");
    }

    private void checkPlayerLocations() {
        if (world != null) {
            Location spawnLocation = world.getSpawnLocation();

            for (Player player : Bukkit.getOnlinePlayers()) {
                Location playerLocation = player.getLocation();

                if (isInRegion(playerLocation, corner1, corner2)) {
                    player.teleport(spawnLocation);
                }
            }
        }
    }

    private void regenerateBlocks() {
        // Replace "your_world_name" with the actual world name

        if (world != null) {
            //Generate Stone
            int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
            int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
            int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());

            int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
            int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
            int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        world.getBlockAt(x, y, z).setType(Material.STONE); // Replace with the desired block type
                    }
                }
            }

            //Generate Ores
            spawnOre(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }

    private void spawnOre(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        //ORE
        for (int i = 0; i < 50; i++) {
            // SELECT ORE TYPE //
            Material oreType = null;
            int amountToSpawn = 0;
            switch(randomInt(0, 5)) {
                case 0:
                    oreType = Material.COAL_ORE;
                    amountToSpawn = randomInt(8, 12);
                    break;
                case 1:
                    oreType = Material.REDSTONE_ORE;
                    amountToSpawn = randomInt(3, 6);
                    break;
                case 2:
                    oreType = Material.REDSTONE_ORE;
                    amountToSpawn = randomInt(3, 8);
                    break;
                case 3:
                    oreType = Material.IRON_ORE;
                    amountToSpawn = randomInt(5, 10);
                    break;
                case 4:
                    oreType = Material.GOLD_ORE;
                    amountToSpawn = randomInt(1, 3);
                    break;
            }
            if (oreType == null) break;
            if (amountToSpawn <= 0) break;

            //Vein SPAWNING LOGIC//
            int oreVeinX = randomInt(minX + 1, maxX);
            int oreVeinY = randomInt(minY+ 1, maxY);
            int oreVeinZ = randomInt(minZ+ 1, maxZ);

            Block centerBlock = world.getBlockAt(oreVeinX, oreVeinY, oreVeinZ); //Set center of ore Vein
            centerBlock.setType(oreType);
            amountToSpawn--;
            // The radius within which blocks will be spawned around the center block
            int radius = 3;
            createVein(radius, oreType, amountToSpawn, centerBlock.getLocation());
        }
    }

    private void createVein(int radius, Material oreType, int amountToSpawn, Location center) {
        int oreSpawned = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.sqrt(x * x + y * y + z * z) <= radius) {
                        Block block = world.getBlockAt(center.getBlockX() + x, center.getBlockY() + y, center.getBlockZ() + z);
                        if (isInRegion(block.getLocation(), corner1, corner2)) {
                            if (block.getType() == Material.STONE) {
                                block.setType(oreType);
                                oreSpawned++;
                                if (oreSpawned >= amountToSpawn) return;
                            }
                        }
                    }
                }
            }
        }
    }
}
