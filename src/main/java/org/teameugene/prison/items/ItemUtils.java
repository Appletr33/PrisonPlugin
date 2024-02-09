package org.teameugene.prison.items;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.teameugene.prison.User;
import org.teameugene.prison.database.Database;
import org.teameugene.prison.enums.CustomItem;
import org.teameugene.prison.enums.Upgrade;
import org.teameugene.prison.enums.UpgradeType;
import org.teameugene.prison.npcs.ArmorSmith;
import org.teameugene.prison.npcs.NPC;
import org.teameugene.prison.npcs.WeaponForger;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.teameugene.prison.Util.RomanParser.intToRoman;
import static org.teameugene.prison.Util.RomanParser.romanToInt;
import static org.teameugene.prison.Util.Utils.getUserFromPlayer;
import static org.teameugene.prison.Util.Utils.getWorldByName;
import static org.teameugene.prison.enums.CustomItem.*;
import static org.teameugene.prison.items.Upgrades.applyContinuousArmorUpgrades;

public class ItemUtils {
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

    private static boolean bIsCustomUpgrade(Upgrade upgrade) {
        return upgrade.getEnchantmentValue() == null;
    }

    public static int getLevel(Upgrade upgrade, ItemStack itemStack) {
        if (bIsCustomUpgrade(upgrade)) {
            return getCustomUpgradeLevel(upgrade, itemStack);
        } else {
            return itemStack.getEnchantmentLevel(upgrade.getEnchantmentValue());
        }
    }

    public static void openUpgradeGUI(Player player, UpgradeType upgradeType) {
        String windowName = "";

        if (upgradeType.equals(UpgradeType.TOOL))
            windowName = "Tool Upgrade";
        else if (upgradeType.equals(UpgradeType.ARMOR))
            windowName = "Armor Upgrade";


        Inventory upgradeGUI = Bukkit.createInventory(player, 9, windowName);
        String lorePre = "Cost: ";
        String lorePost= ChatColor.WHITE + " points";

        int guiSlot = 0;
        for (Upgrade upgrade : Upgrade.values()) {
            int level = 0;
            CustomItem customItem = null;
            if (upgradeType.equals(UpgradeType.TOOL)) {
                switch (upgrade.getUpgradeType()) {
                    case SWORD -> {
                        customItem = COSMIC_SWORD;
                    }
                    case BOW -> {
                        customItem = COSMIC_BOW;
                    }
                    case PICKAXE -> {
                        customItem = COSMIC_PICKAXE;
                    }
                    case TOOL -> {
                        customItem = COSMIC_SWORD;
                    }
                }
            }

            else if (upgradeType.equals(UpgradeType.ARMOR)){
                switch (upgrade.getUpgradeType()) {
                    case HELMET -> {
                        customItem = COSMIC_HELMET;
                    }
                    case CHESTPLATE -> {
                        customItem = COSMIC_CHESTPLATE;
                    }
                    case LEGGINGS -> {
                        customItem = COSMIC_LEGGINGS;
                    }
                    case BOOTS -> {
                        customItem = COSMIC_BOOTS;
                    }
                    case ARMOR -> {
                        customItem = COSMIC_BOOTS;
                    }
                }
            }

            if (customItem != null) {
                level = getLevel(upgrade, getCustomItemFromPlayersInventory(player, customItem));
                String lore;
                if (upgrade.getMaxLevel() >= level + 1) {
                    lore = (lorePre + ChatColor.GREEN + determineCost(upgrade, level) + lorePost);
                } else {
                    lore = ChatColor.RED + "MAXED";
                }
                upgradeGUI.setItem(guiSlot, createItemGUI(upgrade.getColor() + upgrade.getStringValue(), lore, upgrade.getMaterial()));
                guiSlot++;
            }
        }

        // Open the GUI for the player
        player.openInventory(upgradeGUI);
    }

    private static int checkLoreLevel(String lore, String enchantName) {
        String restOfString = lore.substring((ChatColor.WHITE + enchantName + " ").length()).trim(); //Chatcolor needs to be factored in i guesssss
        try {
            return romanToInt(restOfString);
        } catch (NumberFormatException e) {
            throw e;
        }
    }

    private static int getCustomUpgradeLevel(Upgrade upgrade, ItemStack pickaxe) {
        if (pickaxe.hasItemMeta()) {
            if (pickaxe.getItemMeta().hasLore()) {
                List<String> lore = pickaxe.getItemMeta().getLore();
                if (lore != null) {
                    for (String enchant: lore) {
                        if (enchant.contains(upgrade.getStringValue())) {
                            return checkLoreLevel(enchant, upgrade.getStringValue());
                        }
                    }
                }
            }
        }
        return 0;
    }

    public static ItemStack createItemGUI(String upgradeName, String lore, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(upgradeName);
        itemMeta.setLore(Collections.singletonList(lore));
        item.setItemMeta(itemMeta);
        return item;
    }

    private static long determineCost(Upgrade purchasedUpgrade, int currentLevel) {
        if (currentLevel < 3) {
            return (long) purchasedUpgrade.getCostMultiplier() * currentLevel * 100 + 100;
        } else {
            return (long) purchasedUpgrade.getCostMultiplier() * purchasedUpgrade.getCostMultiplier() * currentLevel * 100 + 100;
        }
    }

    public static void handleTeleportationAction(Player player, String destination, Database database) {
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

    private static void appendCustomLore(ItemStack item, String newLore) {
        if (item != null && item.hasItemMeta()) {
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta.hasLore()) {
                // Get the existing lore
                List<String> lore = itemMeta.getLore();
                // Add the new lore
                lore.add(newLore);
                // Set the updated lore back to the item
                itemMeta.setLore(lore);
                item.setItemMeta(itemMeta);
            } else {
                // If the item doesn't have lore, create a new lore list and add the new lore
                List<String> lore = new ArrayList<>();
                lore.add(newLore);
                itemMeta.setLore(lore);
                item.setItemMeta(itemMeta);
            }
        }
    }

    public static void removeCustomLoreLine(ItemStack item, String searchString) {
        if (item != null && item.hasItemMeta()) {
            ItemMeta itemMeta = item.getItemMeta();

            if (itemMeta.hasLore()) {
                List<String> lore = itemMeta.getLore();

                // Iterate over the lore and remove the line containing the search string
                for (String line : lore) {
                    if (line.contains(searchString)) {
                        lore.remove(line);
                        break; // Stop iterating after removing the line
                    }
                }

                // Set the updated lore back to the item
                itemMeta.setLore(lore);
                item.setItemMeta(itemMeta);
            }
        }
    }

    public static void overwriteCustomLoreLine(ItemStack item, String searchString, String newString) {
        if (item != null && item.hasItemMeta()) {
            ItemMeta itemMeta = item.getItemMeta();

            if (itemMeta.hasLore()) {
                List<String> lore = itemMeta.getLore();

                // Iterate over the lore to find and overwrite the line containing the search string
                for (int i = 0; i < lore.size(); i++) {
                    if (lore.get(i).contains(searchString)) {
                        lore.set(i, newString); // Overwrite the line with the new string
                        break; // Stop iterating after overwriting the line
                    }
                }

                // Set the updated lore back to the item
                itemMeta.setLore(lore);
                item.setItemMeta(itemMeta);
            }
        }
    }

    private static void applyCustomUpgrade(ItemStack item, ChatColor ChatColor, Upgrade upgrade, int level) {
        if (level > 1) { // enchant already exists
            overwriteCustomLoreLine(item, upgrade.getStringValue(), ChatColor + upgrade.getStringValue() + " " + intToRoman(level));
        } else { // enchant needs to be added
            appendCustomLore(item, ChatColor + upgrade.getStringValue() + " " + intToRoman(level));
        }
    }

    public static void handleUpgrade(Player player, String upgradeName, ArrayList<User> connectedPlayers) {
        // Get the player's pickaxe
        Upgrade upgrade = Objects.requireNonNull(Upgrade.fromStringValue(upgradeName.substring(2))); //Remove Coloring from clicked upgrade and get the upgrade from that string value

        ArrayList<ItemStack> itemsToUpgrade = new ArrayList<>();
        ArrayList<ItemStack> customItems = getAllCustomItemsFromPlayersInventory(player);

        for (ItemStack item : customItems ) {
            if (itemStackToCustomItem(item).getUpgradeGroup() == upgrade.getUpgradeType())
                itemsToUpgrade.add(item);
            else if ((itemStackToCustomItem(item).getUpgradeType() == upgrade.getUpgradeType()))
                itemsToUpgrade.add(item);
        }

        ChatColor npcNameColor = ChatColor.WHITE;
        NPC npcInteracting = null;

        if (UpgradeType.getUpgradeGroup(upgrade) == UpgradeType.TOOL) {
            npcInteracting = WeaponForger.getInstance();
            npcNameColor = ChatColor.GREEN;
        }
        else if (UpgradeType.getUpgradeGroup(upgrade) == UpgradeType.ARMOR){
            npcInteracting = ArmorSmith.getInstance();
            npcNameColor = ChatColor.DARK_PURPLE;
        }

        String message = "";
        int level = getLevel(upgrade, itemsToUpgrade.get(0));
        long cost = determineCost(upgrade, level);
        User connectedPlayer = getUserFromPlayer(player, connectedPlayers);
        if (connectedPlayer != null) {
            if (upgrade.getMaxLevel() >= level + 1) {
                if (connectedPlayer.subtractPoints(cost)) {
                    message = "Upgrade Applied!";
                    for (ItemStack item : itemsToUpgrade) {
                        if (bIsCustomUpgrade(upgrade)) {
                            applyCustomUpgrade(item, upgrade.getColor(), upgrade, level + 1);
                        } else {
                            item.addUnsafeEnchantment(upgrade.getEnchantmentValue(), level + 1);
                        }
                    }
                    applyContinuousArmorUpgrades(player);
                    openUpgradeGUI(player, UpgradeType.getUpgradeGroup(upgrade));
                } else {
                    message = "You Cannot Afford This Upgrade! You need §c" + (cost - connectedPlayer.getPoints()) + "§f" + " more points!";
                    player.closeInventory();
                }
                } else {
                    message = "Max Level For This Upgrade Reached!";
                    player.closeInventory();
            }
        }
        npcInteracting.sendMessage(player, npcNameColor, message);
    }


    public static ArrayList<Upgrade> getItemUpgrades(ItemStack item) {
        ArrayList<Upgrade> upgradeList = new ArrayList<>();
        ArrayList<Upgrade> availableUpgrades = Upgrade.getCustomEnchants();

        if (item != null && item.hasItemMeta()) {
            ItemMeta itemMeta = item.getItemMeta();

            if (itemMeta.hasLore()) {
                for (String loreLine: itemMeta.getLore()) {
                    for (Upgrade upgrade : availableUpgrades) {
                        if (loreLine.contains(upgrade.getStringValue())) {
                            upgradeList.add(upgrade);
                        }
                    }
                }
            }
        }

        return upgradeList;
    }
}
