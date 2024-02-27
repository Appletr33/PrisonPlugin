package org.teameugene.prison.items;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.teameugene.prison.Prison;
import org.teameugene.prison.Util.User;
import org.teameugene.prison.enums.Upgrade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.teameugene.prison.Util.Utils.*;
import static org.teameugene.prison.items.ItemUtils.getItemUpgrades;
import static org.teameugene.prison.items.ItemUtils.getLevel;

public class Upgrades {

    public static void applyMiningUpgrades( ArrayList<User> connectedPlayers, Player player, ItemStack itemUsed, Block brokenBlock) {
        ArrayList<Upgrade> itemUpgrades = getItemUpgrades(itemUsed);

        for (Upgrade upgrade : itemUpgrades) {
            if (brokenBlock != null) {
                if (upgrade.equals(Upgrade.ATOMIC_DETONATE)) {
                    if (brokenBlock.getType() == Material.STONE) {
                        int level = getLevel(upgrade, itemUsed);
                        getUserFromPlayer(player, connectedPlayers).addPoints(detonateBlocks(brokenBlock, level, player, connectedPlayers) - 1); // -1 cause one of the blocks we broke is the original one

                    }
                }
            }
            if (upgrade.equals(Upgrade.SPEED)) {
                int level = getLevel(upgrade, itemUsed);
                speedUpgrade(player, level);
            }
            if (upgrade.equals(Upgrade.JUMP)) {
                int level = getLevel(upgrade, itemUsed);
                jumpUpgrade(player, level);
            }
        }
    }

    public static void applySwordUpgrades(Player player, ItemStack sword) {
        ArrayList<Upgrade> itemUpgrades = getItemUpgrades(sword);
        for (Upgrade upgrade : itemUpgrades) {
            if (upgrade.equals(Upgrade.SPEED)) {
                int level = getLevel(upgrade, sword);
                speedUpgrade(player, level);
            }
        }
    }

    public static void applyContinuousArmorUpgrades(Player player) {
        ItemStack[] armorItems = player.getInventory().getArmorContents();
        Map<Upgrade, Integer> armorUpgrades = new HashMap<>();

        // Collect all armor upgrades and their levels
        for (ItemStack armorPiece : armorItems) {
            for (Upgrade upgrade : getItemUpgrades(armorPiece)) {
                armorUpgrades.put(upgrade, getLevel(upgrade, armorPiece));
            }
        }

        for (Map.Entry<Upgrade, Integer> entry : armorUpgrades.entrySet()) {
            Upgrade upgrade = entry.getKey();
            int level = entry.getValue();

            if (upgrade.equals(Upgrade.GODLY_OVERLOAD)) {
                double maxHealth = 20 + 20 * (5 * 0.1 * level);
                setPlayerMaxHealth(player, maxHealth);
            }
        }
    }

    private static int detonateBlocks(Block brokenBlock, int level, Player player, ArrayList<User> connectedPlayers) {
        World world = brokenBlock.getWorld();
        Location corner1 = Prison.mine.corner1;
        Location corner2 = Prison.mine.corner2;

        BlockFace blockFace = determineBlockFace(player);
        return mineSquare(player, blockFace, brokenBlock, level, corner1, corner2);
    }

    private static void speedUpgrade(Player player, int level) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5 * 20, level - 1));
    }

    private static void jumpUpgrade(Player player, int level) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 10 * 20, level + 2));
    }
}


