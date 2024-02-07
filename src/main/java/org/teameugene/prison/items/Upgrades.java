package org.teameugene.prison.items;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.teameugene.prison.Prison;
import org.teameugene.prison.User;

import java.util.ArrayList;

import static org.teameugene.prison.Util.Utils.*;

public class Upgrades {
    public static int detonateBlocks(Block brokenBlock, int level, Player player, ArrayList<User> connectedPlayers) {
        World world = brokenBlock.getWorld();
        Location corner1 = Prison.corner1;
        Location corner2 = Prison.corner2;

        BlockFace blockFace = determineBlockFace(player);
        return mineSquare(player, blockFace, brokenBlock, level, corner1, corner2);
    }

    public static void speedUpgrade(Player player, int level) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5 * 20, level - 1));
    }
}


