package org.teameugene.prison.npcs;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.teameugene.prison.enums.UpgradeType;

import static org.teameugene.prison.items.ItemUtils.openUpgradeGUI;

public class WeaponForger extends NPC{

    private static NPC instance;
    public WeaponForger(String name, Location location, String skinName) {
        super(name, location, skinName);
        instance = this;
    }

    @Override
    public void interact(Player player) {
        openUpgradeGUI(player, UpgradeType.TOOL);
    }

    public static NPC getInstance() {
        return instance;
    }
}
