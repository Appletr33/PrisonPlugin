package org.teameugene.prison.ship;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;
import org.teameugene.prison.Prison;
import org.teameugene.prison.Util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.teameugene.prison.Util.Utils.getUserFromPlayer;
import static org.teameugene.prison.items.ItemUtils.createItemGUI;

public class Radar extends Serialize implements GameObject {
    @Serializable
    public boolean active;
    @Serializable
    private Location location;
    @Serializable
    private String ownerUUID;

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
        for (Player player : location.getWorld().getPlayers()) { //TODO eventually check if party members are on instead of just player
            if (player.getUniqueId().toString().equals(ownerUUID)) {
                playSound();
            }
        }
    }

    private void playSound() {
        if (active) {
            for (Player player : location.getWorld().getPlayers()) {
                if (Utils.isInRegion(player.getLocation(), location.clone().subtract(10, 10, 10), location.clone().add(10, 10, 10))) {
                    if (!subscribedPlayers.containsKey(player.getName())) {
                        subscribedPlayers.put(player.getName(), SoundSystem.playContinuousSound(location, radarSearchingSound, 100f, 1f, player));
                        Prison.getInstance().getLogger().info("Player sounds!!!");
                    }
                } else {
                    if (subscribedPlayers.containsKey(player.getName())) {
                        subscribedPlayers.get(player.getName()).cancel();
                        subscribedPlayers.remove(player.getName());
                        SoundSystem.stopSound(player, radarSearchingSound);
                        Prison.getInstance().getLogger().info("Stopping sounds!!!");
                    }
                }
            }
        } else {
            for (String uuid : subscribedPlayers.keySet()) {
               Bukkit.getPlayer(uuid).stopSound(radarSearchingSound);
                subscribedPlayers.get(uuid).cancel();
            }
            subscribedPlayers.clear();
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
