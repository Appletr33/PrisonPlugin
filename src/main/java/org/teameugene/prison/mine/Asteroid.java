package org.teameugene.prison.mine;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.teameugene.prison.Prison;
import org.teameugene.prison.Util.BytePacking;
import org.teameugene.prison.Util.Utils;
import org.teameugene.prison.enums.Difficulty;
import org.teameugene.prison.enums.Ore;
import org.teameugene.prison.ship.Schematic;
import org.teameugene.prison.ship.Ship;

import javax.print.attribute.standard.PrinterURI;
import java.util.ArrayList;
import java.util.UUID;

public class Asteroid {
    private final int id;
    private final float positionX;
    private final Difficulty difficulty;
    private final int radius;
    private final ArrayList<Ore> oreTypes;

    public Asteroid(int id, float positionX, Difficulty difficulty, int radius, ArrayList<Ore> oreTypes) {
        this.id = id;
        this.positionX = positionX;
        this.difficulty = difficulty;
        this.radius = radius;
        this.oreTypes = oreTypes;
    }

    public static void generate(UUID uuid) {
        //Calculate asteroid data
        float posX = Prison.database.findAsteroidWithLargestPositionX();
        if (posX == -1)  // remove  > 0 when testing complete || posX > 0
            return;
        posX += 500;
        int radius = Utils.randomInt(8, 15);
        Difficulty[] difficulties = Difficulty.values();
        Difficulty difficulty = difficulties[Utils.randomInt(0, difficulties.length)];
        Ore[] ores = Ore.values();
        int numOresToAdd = Utils.randomInt(1, 4); // Generates a random integer between 1 and 3 (inclusive)
        // Initialize the chosenOres array with the selected size
        Ore[] chosenOres = new Ore[numOresToAdd];
        // Randomly select and add ores to the chosenOres array
        for (int i = 0; i < numOresToAdd; i++) {
            chosenOres[i] = ores[Utils.randomInt(0, ores.length)];
        }
        spawnAsteroid(new Location(Utils.getWorldByName(Prison.asteroidWorldName), posX, 70f, 0f), radius, difficulty, chosenOres, uuid);

        //Let the player know that an asteroid has been found
        Player player = Bukkit.getPlayer(uuid);
        if (player != null)
            player.sendMessage("Asteroid Found!");
    }

    private static void spawnAsteroid(Location location, int radius, Difficulty difficulty, Ore[] oreTypes, UUID uuid) {
        byte[] oreBytes = Ore.convertOresToBytes(oreTypes);
        long ore = BytePacking.packBytes(oreBytes);
        Prison.database.addAsteroid((float)location.getX(), difficulty.ordinal(), radius, ore, uuid);

        generateStoneBall(location.getWorld(), (int)location.getX(), (int)location.getY(), (int)location.getZ(), radius);
        spawnOre(location, oreTypes, radius);
        spawnEnemies(location, difficulty, radius);
        spawnMiningShip(location, radius);
    }

    private static void spawnEnemies(Location center, Difficulty difficulty, int radius) {
        for (int i = 0; i < difficulty.ordinal() * 3; i++) {
            float spawnX = (float) Utils.randomInt((int)center.getX() - radius + 1, (int)center.getX() + radius - 1);
            float spawnZ = (float) Utils.randomInt((int)center.getZ() - radius + 1, (int)center.getZ() + radius - 1);
            float spawnY = (float) center.getY() + radius + 1;

            //TODO ADD CUSTOM ENTITIES TO SPAWN
            center.getWorld().spawnEntity(new Location(center.getWorld(), spawnX, spawnY, spawnZ), EntityType.ZOMBIE);
        }
    }

    private static void spawnMiningShip(Location center, int radius) {
        Schematic schematic = Schematic.schematics.get(Ship.miningShipSchematicName);
        schematic.paste(new Location(center.getWorld(), center.getX() + radius + 10, center.getY() + radius, center.getZ()));
    }

    private static void spawnOre(Location center, Ore[] oresToSpawn, int radius) {
        for (int i = 0; i < oresToSpawn.length; i++) {
            int veinsToSpawn = Utils.randomInt(2, radius-4);

            for (int x = 0; x < veinsToSpawn; x++) {
                int veinCenterX =  Utils.randomInt((int)center.getX() - radius, (int)center.getX() + radius);
                int veinCenterZ =  Utils.randomInt((int)center.getZ() - radius + 1, (int)center.getZ() + radius - 1);
                int veinCenterY =  Utils.randomInt((int)center.getY() - radius + 1, (int)center.getY() + radius - 1);

                int j = 1; int k = 1; int l = 1;
                for (int z = 0; z < Utils.randomInt(5, 10); z++) {
                    double r = Prison.random.nextDouble();

                    if (r < 0.333) {
                        center.getWorld().getBlockAt(veinCenterX + j, veinCenterY, veinCenterZ).setType(oresToSpawn[i].getMaterial());
                        j++;
                    }

                    else if (r > 0.666) {
                        center.getWorld().getBlockAt(veinCenterX, veinCenterY + k, veinCenterZ).setType(oresToSpawn[i].getMaterial());
                        k++;
                    }
                    else {
                        center.getWorld().getBlockAt(veinCenterX, veinCenterY, veinCenterZ + l).setType(oresToSpawn[i].getMaterial());
                        l++;
                    }
                }
            }
        }
    }

    private static void generateStoneBall(World world, int centerX, int centerY, int centerZ, int radius) {
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = centerY - radius; y <= centerY + radius; y++) {
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    double distanceSquared = Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2) + Math.pow(z - centerZ, 2);
                    if (distanceSquared <= Math.pow(radius, 2)) {
                        Block block = world.getBlockAt(x, y, z);
                        block.setType(Material.STONE); // Set the block to stone
                    }
                }
            }
        }
    }

    public int getId() {
        return id;
    }

    public float getPositionX() {
        return positionX;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getRadius() {
        return radius;
    }

    public ArrayList<Ore> getOreTypes() {
        return oreTypes;
    }
}
