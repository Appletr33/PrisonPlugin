package org.teameugene.prison.Util;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
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
import java.util.ArrayList;

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
}