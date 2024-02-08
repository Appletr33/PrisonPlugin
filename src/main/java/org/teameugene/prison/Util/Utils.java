package org.teameugene.prison.Util;

import net.minecraft.network.protocol.Packet;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.teameugene.prison.Prison;
import org.teameugene.prison.User;
import org.teameugene.prison.database.Database;
import org.teameugene.prison.mine.Schematic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import static org.teameugene.prison.scoreboard.ScoreBoard.displayScoreboard;

public class Utils {

    public static void giveItemsToPlayer(Player player) {
        // Give an unbreakable pickaxe in the first slot
        ItemStack unbreakablePickaxe = createUnbreakablePickaxe();
        player.getInventory().setItem(0, unbreakablePickaxe);

        // Give a redstone torch in the last slot
        ItemStack redstoneTorch = createRedstoneTorch();
        player.getInventory().setItem(8, redstoneTorch);
        player.getInventory().setItem(1, createCosmicSword());
    }

    private static ItemStack createUnbreakablePickaxe() {
        ItemStack pickaxe = new ItemStack(Material.NETHERITE_PICKAXE);
        ItemMeta pickaxeMeta = pickaxe.getItemMeta();
        // Set custom name
        pickaxeMeta.setDisplayName("Cosmic Pickaxe");
        // Set lore
        pickaxeMeta.setLore(java.util.Arrays.asList("Bestowed upon you by the heavens"));
        // Add enchantment for durability
        pickaxe.addEnchantment(Enchantment.DURABILITY, 3);

        pickaxe.setItemMeta(pickaxeMeta);

        return pickaxe;
    }

    private static ItemStack createCosmicSword() {
        ItemStack cosmicSword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta cosmicSwordMeta = cosmicSword.getItemMeta();
        // Set Name
        cosmicSwordMeta.setDisplayName("Cosmic Sword");
        // Set lore
        cosmicSwordMeta.setLore(java.util.Arrays.asList("They say its strength is equal to the might of one's soul"));

        cosmicSword.setItemMeta(cosmicSwordMeta);
        return cosmicSword;
    }

    private static ItemStack createRedstoneTorch() {
        ItemStack redstoneTorch = new ItemStack(Material.REDSTONE_TORCH);
        ItemMeta torchMeta = redstoneTorch.getItemMeta();
        // Set Name
        torchMeta.setDisplayName("Teleporter");
        // Set lore
        torchMeta.setLore(java.util.Arrays.asList("Teleports you instantaneously!"));

        redstoneTorch.setItemMeta(torchMeta);
        return redstoneTorch;
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

    public static void newPlayer(Player player, ArrayList<Schematic> schematicArrayList, String starterShipSchematicName, String shipWorldName, ArrayList<User> connectedPlayers, Database database) {
        player.sendMessage("Welcome to the moon trooper, " + player.getName() + "!");
        Utils.giveItemsToPlayer(player);

        //Create the startership
        double[] pos = database.getPlayerShipCoordinates(player.getUniqueId());
        for (Schematic schematic : schematicArrayList) {
            if (schematic.getName().equals(starterShipSchematicName + ".schem")) {
                schematic.paste(new Location(getWorldByName(shipWorldName), pos[0] - 14, pos[1] - 5, pos[2])); //-14 and -5 are offset values for the schematic so the player spawns on the ship
            }
        }
    }

    public static boolean isInRegion(Location source, Location bound1, Location bound2) {
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
}