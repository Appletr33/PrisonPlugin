package org.teameugene.prison.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.teameugene.prison.User;
import org.teameugene.prison.database.Database;
import org.teameugene.prison.enums.Upgrade;

import java.util.ArrayList;
import java.util.Collections;

import static org.teameugene.prison.mine.Utils.getUserFromPlayer;
import static org.teameugene.prison.mine.Utils.getWorldByName;

public class InventoryListener implements Listener {

    Plugin plugin;
    Database database;
    ArrayList<User> connectedPlayers;

    public InventoryListener(Plugin plugin, Database database, ArrayList<User> connectedPlayers) {
        this.plugin = plugin;
        this.database = database;
        this.connectedPlayers = connectedPlayers;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Pickaxe Upgrade")) {
            event.setCancelled(true);

            // Check if the clicked item is a diamond with the expected lore
            if (event.getCurrentItem() != null &&
                    event.getCurrentItem().getType() == Material.BLAZE_ROD &&
                    event.getCurrentItem().getItemMeta() != null &&
                    event.getCurrentItem().getItemMeta().hasLore() &&
                    event.getCurrentItem().getItemMeta().getLore().get(0).startsWith("Cost:")) {

                // Handle the upgrade logic here
                handleUpgrade((Player) event.getWhoClicked(), event.getCurrentItem().getItemMeta().getDisplayName());
            }
        }

        else if (event.getView().getTitle().equals("Warps")) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null &&
                    (event.getCurrentItem().getType() == Material.END_STONE || event.getCurrentItem().getType() == Material.ELYTRA || event.getCurrentItem().getType() == Material.RED_TERRACOTTA) &&
                    event.getCurrentItem().getItemMeta() != null &&
                    event.getCurrentItem().getItemMeta().hasLore() &&
                    event.getCurrentItem().getItemMeta().getLore().get(0).contains("Teleports you to")) {

                // Handle the upgrade logic here
                handleTeleportationAction((Player) event.getWhoClicked(), event.getCurrentItem().getItemMeta().getDisplayName());
            }
        }
    }

    public static void openTeleportationGUI(Player player) {
        Inventory teleportationGUI = Bukkit.createInventory(player, 9, "Warps");

        //MOON WARP
        ItemStack endStone = new ItemStack(Material.END_STONE);
        ItemMeta endStoneItemMeta = endStone.getItemMeta();
        endStoneItemMeta.setDisplayName("§6Moon");
        endStoneItemMeta.setLore(Collections.singletonList("§fTeleports you to the §6Moon"));
        endStone.setItemMeta(endStoneItemMeta);
        teleportationGUI.setItem(3, endStone);

        //MARS WARP
        ItemStack redTerracotta = new ItemStack(Material.RED_TERRACOTTA);
        ItemMeta redTerracottaItemMeta = redTerracotta.getItemMeta();
        redTerracottaItemMeta.setDisplayName("§4Mars");
        redTerracottaItemMeta.setLore(Collections.singletonList("§fTeleports you to §4Mars"));
        redTerracotta.setItemMeta(redTerracottaItemMeta);
        teleportationGUI.setItem(4, redTerracotta);

        //SHIP WARP
        ItemStack elytra = new ItemStack(Material.ELYTRA);
        ItemMeta elytraItemMeta = elytra.getItemMeta();
        elytraItemMeta.setDisplayName("§2Ship");
        elytraItemMeta.setLore(Collections.singletonList("§fTeleports you to your §2Spaceship"));
        elytra.setItemMeta(elytraItemMeta);
        teleportationGUI.setItem(5, elytra);

        player.openInventory(teleportationGUI);
    }

    public static void openUpgradeGUI(Player player, ItemStack pickaxe) {
        Inventory upgradeGUI = Bukkit.createInventory(player, 9, "Pickaxe Upgrade");

        // EFFICIENCY UPGRADE //

        // Calculate the cost based on the efficiency level of the pickaxe
        int efficiencyLevel = pickaxe.getEnchantmentLevel(Enchantment.DIG_SPEED);
        long cost = determineCost(Upgrade.EFFICIENCY, efficiencyLevel);

        // Create a named diamond with lore indicating the cost
        ItemStack diamond = new ItemStack(Material.BLAZE_ROD);
        ItemMeta diamondMeta = diamond.getItemMeta();
        diamondMeta.setDisplayName("Efficiency Upgrade");
        diamondMeta.setLore(Collections.singletonList("Cost: §6" + cost + "§5 points"));
        diamond.setItemMeta(diamondMeta);

        // Add the diamond to the GUI
        upgradeGUI.setItem(4, diamond);

        //                   //

        // Open the GUI for the player
        player.openInventory(upgradeGUI);
    }

    private static long determineCost(Upgrade purchasedUpgrade, int currentLevel) {
        if (purchasedUpgrade.equals(Upgrade.FORTUNE)) {
            return (long) currentLevel * currentLevel * 50 + 50;
        }
        else if (purchasedUpgrade.equals(Upgrade.EFFICIENCY)) {
            return (long) currentLevel * currentLevel * 100 + 100;
        }
        else if (purchasedUpgrade.equals(Upgrade.ATOMIC_DETONATE)) {
            return (long) currentLevel * currentLevel * currentLevel * 100 + 100;
        }

        return 0;
    }

    private void handleTeleportationAction(Player player, String destination) {
        if (destination.equals("§4Mars")) {
            player.teleport(getWorldByName("mars").getSpawnLocation());
        }
        else if (destination.equals("§6Moon")) {
            player.teleport(getWorldByName("world").getSpawnLocation());
        }
        else if (destination.equals("§2Ship")) {
            double[] pos = database.getPlayerShipCoordinates(player.getUniqueId());
            player.teleport(new Location(getWorldByName("shipworld"), pos[0], pos[1], pos[2]));
        }
    }

    private void handleUpgrade(Player player, String upgradeName) {
        // Get the player's pickaxe
        if (upgradeName.equals("Efficiency Upgrade")) {
            ItemStack pickaxe = player.getInventory().getItemInMainHand();
            int level = pickaxe.getEnchantmentLevel(Enchantment.DIG_SPEED);
            long cost = determineCost(Upgrade.EFFICIENCY, level);

            User connectedPlayer = getUserFromPlayer(player, connectedPlayers);
            if (connectedPlayer != null) {
                if (connectedPlayer.subtractPoints(cost)) {
                    player.sendMessage("Upgrade Applied!");
                    pickaxe.addUnsafeEnchantment(Enchantment.DIG_SPEED, level + 1);
                }
                else {
                    player.sendMessage("You Cannot Afford This Upgrade!, you need §c" + (cost - connectedPlayer.getPoints()) + "§f" + " more points!");
                }
            }
        }

        // Close the GUI
        player.closeInventory();
    }
}
