package org.teameugene.prison.enums;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.Objects;

import static org.teameugene.prison.items.ItemUtils.openTeleportationGUI;
import static org.teameugene.prison.items.ItemUtils.openUpgradeGUI;

public enum CustomItem {
    //todo implement sword, pickaxe, teleported, cosmic boots, cosmic leggings, cosmic chestplate, cosmic helmet

    COSMIC_SWORD("Cosmic Sword", Material.NETHERITE_SWORD, "An Ancient Artifact", UpgradeType.SWORD, UpgradeType.TOOL),
    COSMIC_PICKAXE("Cosmic Pickaxe", Material.NETHERITE_PICKAXE, "An Ancient Artifact", UpgradeType.PICKAXE, UpgradeType.TOOL),
    COSMIC_BOW("Cosmic Bow", Material.BOW, "An Ancient Artifact", UpgradeType.BOW, null),
    COSMIC_HELMET("Cosmic Helmet", Material.LEATHER_HELMET, "Keeps you Safe!", UpgradeType.HELMET, UpgradeType.ARMOR),
    COSMIC_CHESTPLATE("Cosmic Chestplate", Material.LEATHER_CHESTPLATE, "Keeps you Safe!", UpgradeType.CHESTPLATE, UpgradeType.ARMOR),
    COSMIC_LEGGINGS("Cosmic Leggings", Material.LEATHER_LEGGINGS, "Keeps you Safe!", UpgradeType.LEGGINGS, UpgradeType.ARMOR),
    COSMIC_BOOTS("Cosmic Boots", Material.LEATHER_BOOTS, "Keeps you Safe!", UpgradeType.BOOTS, UpgradeType.ARMOR),
    TELEPORTER("Teleporter", Material.REDSTONE_TORCH, "Teleports you Instantaneously!", null, null);


    private final String displayName;
    private final Material material;
    private final String lore;
    private final UpgradeType upgradeType;
    private final UpgradeType upgradeGroup;

    CustomItem(String name, Material material, String lore, UpgradeType upgradeType, UpgradeType upgradeGroup) {
        this.displayName = name;
        this.material = material;
        this.lore = lore;
        this.upgradeType = upgradeType;
        this.upgradeGroup = upgradeGroup;
    }

    public static ItemStack createCustomItem(CustomItem itemToCreate) {
        ItemStack createdItem = new ItemStack(itemToCreate.material);
        ItemMeta createdItemMeta = createdItem.getItemMeta();
        createdItemMeta.setDisplayName(itemToCreate.displayName);
        createdItemMeta.setLore(java.util.Arrays.asList(itemToCreate.lore));
        createdItem.setItemMeta(createdItemMeta);

        // Check if the item is leather armor
        if (createdItem.getType() == Material.LEATHER_HELMET ||
                createdItem.getType() == Material.LEATHER_CHESTPLATE ||
                createdItem.getType() == Material.LEATHER_LEGGINGS ||
                createdItem.getType() == Material.LEATHER_BOOTS) {

            // Convert ItemMeta to LeatherArmorMeta
            LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) createdItemMeta;

            // Set the color of the leather armor to white
            leatherArmorMeta.setColor(org.bukkit.Color.WHITE);

            // Apply the modified LeatherArmorMeta back to the item
            createdItem.setItemMeta(leatherArmorMeta);
        } else {
            // Set regular metadata for non-leather armor items
            createdItem.setItemMeta(createdItemMeta);
        }
        return createdItem;
    }

    public static boolean isCustomItem(ItemStack item) {
        return itemStackToCustomItem(item) != null;
    }

    public static CustomItem itemStackToCustomItem(ItemStack item) {
        if (item == null) return null;

        for (CustomItem customItemToCheckAgainst : CustomItem.values()) {
            if (item.hasItemMeta()) {
                if (Objects.requireNonNull(item.getItemMeta()).hasLore()) {
                    if (item.getItemMeta().getDisplayName().equals(customItemToCheckAgainst.displayName)) {
                        if  (Objects.requireNonNull(item.getItemMeta().getLore()).contains(customItemToCheckAgainst.lore)) {
                            return customItemToCheckAgainst;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static ItemStack getCustomItemFromPlayersInventory(Player player, CustomItem customItemToGet) {
        for (ItemStack itemStack : player.getInventory()) {
            CustomItem item = itemStackToCustomItem(itemStack);
            if (item != null) {
                if (item.equals(customItemToGet))
                    return itemStack;
            }
        }
        return null;
    }

    public static ArrayList<ItemStack> getAllCustomItemsFromPlayersInventory(Player player) {
        ArrayList<ItemStack> customItems = new ArrayList<>();

        for (ItemStack itemStack : player.getInventory()) {
            CustomItem item = itemStackToCustomItem(itemStack);
            if (item != null) {
                if (isCustomItem(itemStack))
                    customItems.add(itemStack);
            }
        }
        return customItems;
    }

    public static void handleToolLogic(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        CustomItem itemUsed = itemStackToCustomItem(player.getInventory().getItemInMainHand());
        if (itemUsed == null) return;

        if (itemUsed.equals(TELEPORTER)) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR) {
                openTeleportationGUI(player);
            }

            else if ((event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                // Cancel the event to prevent placing the redstone torch
                event.setCancelled(true);
                openTeleportationGUI(player);
            }
        }
    }

    public UpgradeType getUpgradeType() {
        return this.upgradeType;
    }

    public UpgradeType getUpgradeGroup() {
        return this.upgradeGroup;
    }

}
