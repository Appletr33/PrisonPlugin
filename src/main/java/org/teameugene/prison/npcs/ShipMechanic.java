package org.teameugene.prison.npcs;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import static org.teameugene.prison.ship.Ship.openShipUpgradeGUI;

public class ShipMechanic extends NPC {
    private static NPC instance;

    public ShipMechanic(String name, Location location, String skinName) {
        super(name, location, skinName);
        instance = this;
    }

    @Override
    public void interact(Player player) {
        openShipUpgradeGUI(player);
    }

    public static NPC getInstance() {
        return instance;
    }
}
