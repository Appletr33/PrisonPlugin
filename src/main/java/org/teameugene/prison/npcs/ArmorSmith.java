package org.teameugene.prison.npcs;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.teameugene.prison.enums.UpgradeType;

import static org.teameugene.prison.items.ItemUtils.openUpgradeGUI;

public class ArmorSmith extends NPC {
    private static NPC instance;

    public ArmorSmith(String name, Location location, String skinName) {
        super(name, location, skinName);
        instance = this;
    }

    @Override
    public void interact(Player player) {
        openUpgradeGUI(player, UpgradeType.ARMOR);
    }

    public static NPC getInstance() {
        return instance;
    }
}
