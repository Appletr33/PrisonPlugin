package org.teameugene.prison.Util;

import net.minecraft.network.protocol.Packet;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.teameugene.prison.Prison;
import org.teameugene.prison.database.Database;
import org.teameugene.prison.enums.CustomItem;
import org.teameugene.prison.ship.Radar;
import org.teameugene.prison.ship.Schematic;
import org.teameugene.prison.ship.Ship;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import static org.teameugene.prison.Util.CustomMaps.getMapItem;
import static org.teameugene.prison.Util.TextEntities.spawnTextEntity;
import static org.teameugene.prison.enums.CustomItem.createCustomItem;
import static org.teameugene.prison.scoreboard.ScoreBoard.displayScoreboard;

public class Utils {

    public static void giveItemsToPlayer(Player player) {
        // Give an unbreakable pickaxe in the first slot
        player.getInventory().setItem(0, createCustomItem(CustomItem.COSMIC_PICKAXE));
        player.getInventory().setItem(1, createCustomItem(CustomItem.COSMIC_SWORD));
        player.getInventory().setItem(2, createCustomItem(CustomItem.COSMIC_BOW));
        player.getInventory().setItem(8, createCustomItem(CustomItem.TELEPORTER));
        player.getInventory().setHelmet(createCustomItem(CustomItem.COSMIC_HELMET));
        player.getInventory().setChestplate(createCustomItem(CustomItem.COSMIC_CHESTPLATE));
        player.getInventory().setLeggings(createCustomItem(CustomItem.COSMIC_LEGGINGS));
        player.getInventory().setBoots(createCustomItem(CustomItem.COSMIC_BOOTS));
    }

    public static World getWorldByName(String worldName) {
        return Bukkit.getWorld(worldName);
    }

    public static void broadcastMessageInWorld(World world, String message) {
        for (Player player : world.getPlayers()) {
            player.sendMessage(message);
        }
    }

    public static User getUserFromPlayer(Player player, ArrayList<User> users) {
        for (User user : users) {
            if (user.getUUID().equals(player.getUniqueId()))
                return user;
        }
        return null;
    }

    public static void updateDatabase(Database database, ArrayList<User> connectedPlayers) {
        if (database.isConnected())
            for (User user : connectedPlayers) {
                database.updatePoints(user.getUUID(), user.getPoints());
            }
    }

    public static void newPlayer(Player player, String starterShipSchematicName, String shipWorldName, ArrayList<User> connectedPlayers, Database database) {
        player.sendMessage("Welcome to the moon trooper, " + player.getName() + "!");
        Utils.giveItemsToPlayer(player);

        //Create the startership
        double[] pos = database.getPlayerShipCoordinates(player.getUniqueId());
        Schematic schematic = Schematic.schematics.get(Ship.starterShipSchematicName);
        schematic.paste(new Location(getWorldByName(shipWorldName), pos[0] - 14 + 27, pos[1] - 5 +6, pos[2] + 9)); //offset values for the schematic so the player spawns on the ship
        Location radarLocation = new Location(getWorldByName(shipWorldName), pos[0], pos[1] + 1, pos[2] -5);
        setItemFrameImage(radarLocation, "control_panel");
        radarLocation.setY(radarLocation.getY() + 0.5);
        radarLocation.setX(radarLocation.getX() + 0.5);
        radarLocation.setZ(radarLocation.getZ() + 0.5);
        spawnTextEntity(radarLocation, "§b§nRight Click", "radar");
        radarLocation.setY(radarLocation.getY() + 0.25);
        spawnTextEntity(radarLocation, "§4§lRadar", "radar");
        Radar radar = new Radar();
        radar.setOwnerUUID(player.getUniqueId().toString());
        radar.setLocation(radarLocation);
        radar.Initialize();
    }

    private static void setItemFrameImage(Location location, String mapName) {
        ItemFrame frame = (ItemFrame) Objects.requireNonNull(location.getWorld()).spawn(
                location.getWorld().getBlockAt(location).getLocation(),
                ItemFrame.class);
        frame.setItem(getMapItem(mapName, location.getWorld()));
    }

    public static boolean isInRegion(Location source, Location bound1, Location bound2) {

        if (source.getWorld() != bound1.getWorld() || bound1.getWorld() != bound2.getWorld() || source.getWorld() != bound2.getWorld())
            return false;

        return source.getX() >= Math.min(bound1.getX(), bound2.getX()) &&
                source.getY() >= Math.min(bound1.getY(), bound2.getY()) &&
                source.getZ() >= Math.min(bound1.getZ(), bound2.getZ()) &&
                source.getX() <= Math.max(bound1.getX(), bound2.getX()) &&
                source.getY() <= Math.max(bound1.getY(), bound2.getY()) &&
                source.getZ() <= Math.max(bound1.getZ(), bound2.getZ());
    }

    public static int mineSquare(Player player, BlockFace blockFace, Block brokenBlock, int radius, Location minebounds1, Location minebounds2) {
        World world = player.getWorld();
        int stoneBroken = 0;

        int startX = brokenBlock.getX() - radius;
        int startY = brokenBlock.getY() - radius;
        int startZ = brokenBlock.getZ() - radius;

        int endX = brokenBlock.getX() + radius;
        int endY = brokenBlock.getY() + radius;
        int endZ = brokenBlock.getZ() + radius;

        switch (blockFace) {
            case NORTH:
                break;
            case SOUTH:
                break;
            case EAST:
            case WEST:
                break;
            case UP:
                break;
            case DOWN:
//                startY = brokenBlock.getY();
//                endY = brokenBlock.getY() + 1;
                break;
        }

        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                for (int z = startZ; z <= endZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType().equals(Material.STONE))
                        if (isInRegion(block.getLocation(), minebounds1, minebounds2)) {
                            block.setType(Material.AIR);
                            stoneBroken++;
                        }
                }
            }
        }
        return stoneBroken;
    }

    public static BlockFace determineBlockFace(Player player) {
        double yaw = (player.getLocation().getYaw() + 360) % 360;
        double pitch = player.getLocation().getPitch();
        if (pitch > 45) {
            return BlockFace.DOWN;
        } else if (pitch < -45) {
            return BlockFace.UP;
        } else {
            if (yaw >= 45 && yaw < 135) {
                return BlockFace.NORTH;
            } else if (yaw >= 135 && yaw < 225) {
                return BlockFace.EAST;
            } else if (yaw >= 225 && yaw < 315) {
                return BlockFace.SOUTH;
            } else if (yaw >= 315 || yaw < 45) {
                return BlockFace.WEST;
            }
        }
        return null;
    }

    public static int randomInt(int low, int high) {
        return Prison.random.nextInt(high-low) + low;
    }

    public static void initPlayer(Database database, Player player, ArrayList<User> connectedPlayers) {
        User user = new User(database.getPoints(player.getUniqueId()), player);
        connectedPlayers.add(user);
        displayScoreboard(player, user.getPoints());
    }

    public static void addItemOrDrop(Player player, ItemStack itemStack) {
        Inventory inventory = player.getInventory();

        // Check if the player's inventory is full
        if (inventory.firstEmpty() != -1) {
            // Inventory has empty slots, add item to inventory
            inventory.addItem(itemStack);
            return; // Exit the function after adding the item
        }

        // Check if there are slots with the same item type but not at a full stack
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack slotItem = inventory.getItem(i);
            if (slotItem != null && slotItem.isSimilar(itemStack) && slotItem.getAmount() < slotItem.getMaxStackSize()) {
                int spaceLeft = slotItem.getMaxStackSize() - slotItem.getAmount();
                if (spaceLeft >= itemStack.getAmount()) {
                    // There is enough space in this slot for the entire item stack
                    slotItem.setAmount(slotItem.getAmount() + itemStack.getAmount());
                    return; // Exit the function after adding the item
                } else {
                    // Add as much of the item as possible to this slot and continue to next slot
                    slotItem.setAmount(slotItem.getMaxStackSize());
                    itemStack.setAmount(itemStack.getAmount() - spaceLeft);
                }
            }
        }

        // If no suitable slot was found, drop the remaining item stack
        player.getWorld().dropItem(player.getLocation(), itemStack);
    }

    public static void sendPacket(Packet<?> packet, Player player) {
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    public static void setValue(Object packet, String fieldName, Object value) {
        try {
            Field field = packet.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(packet, value);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static String getStringFromURL(String url) {
        StringBuilder text = new StringBuilder();
        try {
            Scanner scanner = new Scanner(new URL(url).openStream());
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                while (line.startsWith(" ")) {
                    line = line.substring(1);
                }
                text.append(line);
            }
            scanner.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return text.toString();
    }

    public static Object getPrivateValue(Object instance, String name) {
        Object result = null;
        try {
            Field field = instance.getClass().getDeclaredField(name);
            field.setAccessible(true);
            result = field.get(instance);
            field.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // A method to create a unique key for the custom metadata
    public static NamespacedKey getKey(String key) {
        return new NamespacedKey(Prison.getInstance(), key);
    }

    public static boolean isArmorItem(Material material) {
        return material == Material.LEATHER_HELMET ||
                material == Material.LEATHER_CHESTPLATE ||
                material == Material.LEATHER_LEGGINGS ||
                material == Material.LEATHER_BOOTS ||
                material == Material.IRON_HELMET ||
                material == Material.IRON_CHESTPLATE ||
                material == Material.IRON_LEGGINGS ||
                material == Material.IRON_BOOTS ||
                material == Material.GOLDEN_HELMET ||
                material == Material.GOLDEN_CHESTPLATE ||
                material == Material.GOLDEN_LEGGINGS ||
                material == Material.GOLDEN_BOOTS ||
                material == Material.DIAMOND_HELMET ||
                material == Material.DIAMOND_CHESTPLATE ||
                material == Material.DIAMOND_LEGGINGS ||
                material == Material.DIAMOND_BOOTS ||
                material == Material.NETHERITE_BOOTS ||
                material == Material.NETHERITE_LEGGINGS ||
                material == Material.NETHERITE_CHESTPLATE ||
                material == Material.NETHERITE_HELMET;
    }

    public static void setPlayerMaxHealth(Player player, double maxHealth) {
        // Ensure the max health is within valid range
        maxHealth = Math.max(1, maxHealth); // Ensure maxHealth is not less than 1

        // Set the player's maximum health attribute
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);

        // Set the player's current health to match the new max health
        double currentHealth = player.getHealth();
        if (currentHealth > maxHealth) {
            player.setHealth(maxHealth);
        }
    }

    public static String getSubstringBeforeCharacter(String inputString, char specificCharacter) {
        int index = inputString.indexOf(specificCharacter);
        if (index != -1) {
            return inputString.substring(0, index);
        } else {
            return inputString; // Return the entire string if the character is not found
        }
    }

    public static <K, V> K getKeyByValue(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null; // Return null if the value is not found
    }

    public static String getHash(String input) {
        try {
            // Get a MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Compute the hash of the input string
            byte[] bytes = md.digest(input.getBytes());

            // Convert the byte array to a hexadecimal string
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}