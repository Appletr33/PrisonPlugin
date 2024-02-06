package org.teameugene.prison.mine;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import static org.teameugene.prison.mine.Utils.broadcastMessageInWorld;
import static org.teameugene.prison.mine.Utils.getWorldByName;

public class Mine {

    private long regenationTime = 1;
    String worldName = "world";
    private int warnCount = 0;
    private final int maxWarnCount = 10;
    Plugin plugin;
    World world;

    Location corner1;
    Location corner2;

    public Mine(Plugin plugin) {
        this.plugin = plugin;
        world = Bukkit.getWorld(worldName);
        corner1 = new Location(world, -1729, 20, 768);
        corner2 = new Location(world, -1776, 0, 815);

        registerTasks();
    }

    private void registerTasks() {
        //Register Regeneration Task
        new BukkitRunnable() {
            @Override
            public void run() {
                //1 min warning
                broadcastMessageInWorld(getWorldByName(worldName), "§6New ore will rise from the mine in 60 seconds!");

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
                }.runTaskTimer(plugin, 20 * 50, 20 * 1);

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
        broadcastMessageInWorld(getWorldByName(worldName),"New Ore Arriving in §4" + (maxWarnCount - warnCount) + "§fs!");
    }

    private void regenerateMine() {
        plugin.getLogger().info("[INFO]: Regenerating Mine");
        checkPlayerLocations();
        regenerateBlocks();
        plugin.getLogger().info("[INFO]: Mine Successfully Regenerated");
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

    private boolean isInRegion(Location source, Location bound1, Location bound2) {
        return source.getX() >= Math.min(bound1.getX(), bound2.getX()) &&
                source.getY() >= Math.min(bound1.getY(), bound2.getY()) &&
                source.getZ() >= Math.min(bound1.getZ(), bound2.getZ()) &&
                source.getX() <= Math.max(bound1.getX(), bound2.getX()) &&
                source.getY() <= Math.max(bound1.getY(), bound2.getY()) &&
                source.getZ() <= Math.max(bound1.getZ(), bound2.getZ());
    }

    private void regenerateBlocks() {
        // Replace "your_world_name" with the actual world name

        if (world != null) {
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
        }
    }

}
