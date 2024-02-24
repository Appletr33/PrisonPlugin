package org.teameugene.prison.ship;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;
import org.teameugene.prison.Prison;
import org.teameugene.prison.Util.*;
import org.teameugene.prison.mine.Asteroid;

import java.util.*;

import static org.teameugene.prison.Util.Utils.getUserFromPlayer;
import static org.teameugene.prison.items.ItemUtils.createItemGUI;

public class Radar extends Serialize {
    @Serializable
    public boolean active;
    @Serializable
    private Location location;
    @Serializable
    private String ownerUUID;
    @Serializable
    private double timeSpentScanning = 0;

    Map<String, BukkitTask> subscribedPlayers = new HashMap<>();
    Sound radarSearchingSound = Sound.AMBIENT_UNDERWATER_LOOP;


    public Radar() {
        super();
        active = false;
    }

    public void Initialize() {
        GameObjectManager.radars.put(ownerUUID, this);
    }

    public void setOwnerUUID(String ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public boolean getActive() {
        return active;
    }

    public void toggleActive() {
        active = !active;
    }

    public void tick() {
        playingSound = active;
        playSound();

        timeSpentScanning += GameObjectManager.tickSpeed;
        if (timeSpentScanning > 60) {
            // 3 seconds
            timeSpentScanning = 0;
            Asteroid.generate(Bukkit.getPlayer(ownerUUID));
        }
    }

    private void playSound() {
        if (active) { //TODO: REmove this check later at the cost of performance
            for (Player player : location.getWorld().getPlayers()) {
                User usr = Utils.getUserFromPlayer(player, Prison.connectedPlayers);
                usr.soundSystem.playContinuousSound(location, radarSearchingSound, 100f, 1f, 10, player, this, 10);
            }
        }
    }

    public void openGUI(Player player) {
        Inventory upgradeGUI = Bukkit.createInventory(player, 54, "Radar");
        User user = getUserFromPlayer(player, Prison.connectedPlayers);
        assert user != null;
        if (getActive()) {
            upgradeGUI.setItem(4 + 9, createItemGUI("§a§nActively Scanning", "", Material.GREEN_TERRACOTTA));
        } else {
            upgradeGUI.setItem(4 + 9, createItemGUI("§c§nDeactivated", "", Material.RED_TERRACOTTA));
        }

        upgradeGUI.setItem(53, createItemGUI("§6§oNext", "", Material.ARROW));
        player.openInventory(upgradeGUI);
    }

    public void handleGUI(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() != null &&
                (event.getCurrentItem().getType().equals(Material.GREEN_TERRACOTTA)
                        || event.getCurrentItem().getType().equals(Material.RED_TERRACOTTA))) {
            User user = getUserFromPlayer(player, Prison.connectedPlayers);
            assert user != null;
            toggleActive();
            openGUI(player);
        }
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
