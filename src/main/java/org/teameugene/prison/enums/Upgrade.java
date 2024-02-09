package org.teameugene.prison.enums;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;

public enum Upgrade {
    EFFICIENCY("Efficiency", Enchantment.DIG_SPEED, ChatColor.GREEN, Material.BLAZE_ROD, 2, 10, UpgradeType.PICKAXE),
    FORTUNE("Fortune", Enchantment.LOOT_BONUS_BLOCKS, ChatColor.AQUA, Material.DIAMOND, 2, 10, UpgradeType.PICKAXE),
    ATOMIC_DETONATE("Atomic Detonate", null, ChatColor.RED, Material.TNT, 3, 5, UpgradeType.PICKAXE),
    SPEED("Speed", null, ChatColor.LIGHT_PURPLE, Material.SUGAR, 2, 3, UpgradeType.TOOL),
    JUMP("Jump", null, ChatColor.YELLOW, Material.RABBIT_FOOT, 3, 3, UpgradeType.PICKAXE),
    GODLY_OVERLOAD("Godly Overload", null, ChatColor.LIGHT_PURPLE, Material.TOTEM_OF_UNDYING, 3, 3, UpgradeType.CHESTPLATE),
    PROTECTION("Protection", Enchantment.PROTECTION_ENVIRONMENTAL, ChatColor.DARK_AQUA, Material.IRON_CHESTPLATE, 2, 5, UpgradeType.ARMOR),
    SHARPNESS("Sharpness", Enchantment.DAMAGE_ALL, ChatColor.DARK_GREEN, Material.DIAMOND_SWORD, 2, 10, UpgradeType.SWORD);

    private final String stringValue;
    private final Enchantment enchantment;
    private final ChatColor color;
    private final Material material;
    private final int costMultiplier;
    private final int maxLevel;
    private final UpgradeType upgradeType;

    Upgrade(String stringValue, Enchantment enchantment, ChatColor color, Material material, int costMultiplier, int maxLevel, UpgradeType upgradeType) {
        this.stringValue = stringValue;
        this.enchantment = enchantment;
        this.material = material;
        this.color = color;
        this.costMultiplier = costMultiplier;
        this.maxLevel = maxLevel;
        this.upgradeType = upgradeType;
    }

    public String getStringValue() {
        return stringValue;
    }

    public int getCostMultiplier() {
        return this.costMultiplier;
    }

    public int getMaxLevel() {
        return this.maxLevel;
    }

    public Material getMaterial() {
        return this.material;
    }

    public ChatColor getColor() {
        return this.color;
    }

    public Enchantment getEnchantmentValue() {
        return enchantment;
    }

    public static Upgrade fromStringValue(String stringValue) {
        for (Upgrade enumValue : Upgrade.values()) {
            if (enumValue.stringValue.equals(stringValue)) {
                return enumValue;
            }
        }
        return null;
    }

    public static ArrayList<Upgrade> getCustomEnchants() {
        ArrayList<Upgrade> customUpgrades = new ArrayList<>();
        for (Upgrade upgrade : Upgrade.values()) {
            if (upgrade.enchantment == null) {
                customUpgrades.add(upgrade);
            }
        }
        return customUpgrades;
    }

    public UpgradeType getUpgradeType() {
        return this.upgradeType;
    }
}
