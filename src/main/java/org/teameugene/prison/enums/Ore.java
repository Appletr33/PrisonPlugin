package org.teameugene.prison.enums;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;

public enum Ore {
    GOLD(Material.GOLD_ORE, Material.RAW_GOLD, Material.GOLD_INGOT, 60, 0),
    DIAMOND(Material.DIAMOND_ORE, Material.DIAMOND, Material.DIAMOND,80, 1),
    NETHERITE(Material.ANCIENT_DEBRIS, Material.ANCIENT_DEBRIS, Material.NETHERITE_INGOT,90, 1),
    IRON(Material.IRON_ORE, Material.RAW_IRON, Material.IRON_INGOT,40, 0),
    REDSTONE(Material.REDSTONE_ORE, Material.REDSTONE, Material.REDSTONE, 20, 0),
    COAL(Material.COAL_ORE, Material.COAL, Material.COAL, 10, 0);

    private final Material material;
    private final int rarity;
    private final int toughness;
    private final Material drop;
    private final Material itemForm;

    Ore(Material material, Material drop, Material itemForm, int rarity, int toughness) {
        this.material = material;
        this.rarity = rarity;
        this.toughness = toughness;
        this.drop = drop;
        this.itemForm = itemForm;
    }

    public Material getMaterial() {
        return this.material;
    }
    public int getRarity() {
        return this.rarity;
    }

    public int getToughness() {
        return this.toughness;
    }

    public Material getDrop() {
        return this.drop;
    }

    public Material getItemForm() {
        return this.itemForm;
    }

    public static ArrayList<Ore> getOres() {
        return new ArrayList<>(Arrays.asList(Ore.values()));
    }

    public static ItemStack oreToItem(ItemStack itemStack) {
        for (Ore ore: Ore.getOres()) {
            if (itemStack.getType().equals(ore.getDrop())) {
                return new ItemStack(ore.getItemForm(), itemStack.getAmount());
            }
        }
        return itemStack;
    }

    public static byte[] convertOresToBytes(Ore[] ores) {
        byte[] bytes = new byte[ores.length];
        for (int i = 0; i < ores.length; i++) {
            bytes[i] = (byte) (ores[i].ordinal() + 1);
        }
        return bytes;
    }

}
