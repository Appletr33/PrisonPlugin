package org.teameugene.prison.mine;

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
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.teameugene.prison.Prison;
import org.teameugene.prison.User;
import org.teameugene.prison.database.Database;

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
}