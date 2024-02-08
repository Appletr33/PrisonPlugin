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
import org.bukkit.util.Vector;
import org.teameugene.prison.Prison;
import org.teameugene.prison.User;
import org.teameugene.prison.enums.Upgrade;

import java.util.ArrayList;

import static org.teameugene.prison.Util.Utils.*;
import static org.teameugene.prison.items.ItemUtils.getItemUpgrades;
import static org.teameugene.prison.items.ItemUtils.getLevel;

public class Upgrades {

    public static void applyUpgrades( ArrayList<User> connectedPlayers, Player player, ItemStack itemUsed, Block brokenBlock) {
        ArrayList<Upgrade> itemUpgrades = getItemUpgrades(itemUsed);

        for (Upgrade upgrade : itemUpgrades) {
            if (brokenBlock != null) {
                if (upgrade.equals(Upgrade.ATOMIC_DETONATE)) {
                    if (brokenBlock.getType() == Material.STONE) {
                        int level = getLevel(upgrade, itemUsed);
                        getUserFromPlayer(player, connectedPlayers).addPoints(detonateBlocks(brokenBlock, level, player, connectedPlayers));

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

    private static int detonateBlocks(Block brokenBlock, int level, Player player, ArrayList<User> connectedPlayers) {
        World world = brokenBlock.getWorld();
        Location corner1 = Prison.corner1;
        Location corner2 = Prison.corner2;

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


