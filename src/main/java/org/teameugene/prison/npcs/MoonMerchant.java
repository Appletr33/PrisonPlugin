package org.teameugene.prison.npcs;



import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.teameugene.prison.Prison;
import org.teameugene.prison.enums.Ore;

import java.util.ArrayList;
import java.util.UUID;

import static org.teameugene.prison.Util.Utils.getUserFromPlayer;

public class MoonMerchant extends NPC {

    private static NPC instance;

    public MoonMerchant(String name, Location location, String skinName) {
        super(name, location, skinName);
        instance = this;
    }

    @Override
    public void interact(Player player) {
        long creditsEarned = 0;
        ArrayList<Ore> ores = Ore.getOres();

        for (ItemStack itemStack : player.getInventory()) {
            if (itemStack != null)
                for (Ore ore : ores) {
                    if (itemStack.getType().equals(ore.getItemForm())) {
                        player.getInventory().remove(itemStack);
                        creditsEarned += (long) itemStack.getAmount() * ore.getRarity();
                    }
                }
        }

        if (creditsEarned > 0) {
            getUserFromPlayer(player, Prison.connectedPlayers).addPoints(creditsEarned);
            player.sendMessage(ChatColor.GOLD + "<Moon Merchant>" + ChatColor.WHITE + " Ores Sold for " + ChatColor.GREEN + creditsEarned + ChatColor.WHITE + " credits!");

        }
        else
            player.sendMessage(ChatColor.GOLD + "<Moon Merchant>" + ChatColor.WHITE + " Head to the mine and get me some ores, will ya?!");
    }

    public static NPC getInstance() {
        return instance;
    }

}
