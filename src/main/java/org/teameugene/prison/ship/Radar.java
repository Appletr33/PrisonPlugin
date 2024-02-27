package org.teameugene.prison.ship;

import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.world.level.border.WorldBorder;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.teameugene.prison.Prison;
import org.teameugene.prison.Util.*;
import org.teameugene.prison.enums.Ore;
import org.teameugene.prison.mine.Asteroid;

import java.util.*;

import static org.teameugene.prison.Util.Utils.getUserFromPlayer;
import static org.teameugene.prison.Util.Utils.getWorldByName;
import static org.teameugene.prison.items.ItemUtils.createItemGUI;
import static org.teameugene.prison.items.ItemUtils.handleTeleportationAction;

public class Radar extends Serialize {
    @Serializable
    public boolean active;
    @Serializable
    private Location location;
    @Serializable
    private String ownerUUID;
    @Serializable
    private double timeSpentScanning = 0;

    Sound radarSearchingSound = Sound.ENTITY_DOLPHIN_AMBIENT_WATER;

    public Radar() {
        super();
        active = false;
    }

    public void Initialize() {
        GameObjectManager.radars.put(ownerUUID, this);
        GameObjectManager.gameObjects.add(this);
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

        if (active) {
            timeSpentScanning += GameObjectManager.tickSpeed;
            if (timeSpentScanning > 600 * 2) {
                // 3 seconds
                timeSpentScanning = 0;
                Asteroid.generate(UUID.fromString(ownerUUID));
            }
        }
    }

    private void playSound() {
        if (active) { //TODO: REmove this check later at the cost of performance
            for (Player player : location.getWorld().getPlayers()) {
                User usr = Utils.getUserFromPlayer(player, Prison.connectedPlayers);
                usr.soundSystem.playContinuousSound(location, radarSearchingSound, 100f, 1f, 0.1f, player, this, 10);
            }
        }
    }

    public void openGUI(Player player, int page) {
        Inventory upgradeGUI = Bukkit.createInventory(player, 54, "Radar");
        User user = getUserFromPlayer(player, Prison.connectedPlayers);
        assert user != null;
        if (getActive()) {
            upgradeGUI.setItem(4, createItemGUI("§a§nActively Scanning", "", Material.GREEN_TERRACOTTA));
        } else {
            upgradeGUI.setItem(4, createItemGUI("§c§nDeactivated", "", Material.RED_TERRACOTTA));
        }
        upgradeGUI.setItem(5, createItemGUI("§6§oNext", "", Material.ARROW));

        ArrayList<Asteroid> asteroids = Prison.database.getAsteroidsByPlayerUUID(player.getUniqueId());

        int guiIndex = 9 + 1;

        for (Asteroid asteroid : asteroids) {
            // Set Item Lore
            ArrayList<String> lore = new ArrayList<>();
            lore.add("§fDifficulty: " + asteroid.getDifficulty().getChatColor() + asteroid.getDifficulty().toString().toUpperCase());
            lore.add("§fRadius: " + "§d" + asteroid.getRadius());
            lore.add("");
            lore.add("§fOre:");

            Map<Ore, Integer> oreMap = new HashMap<>();
            for (Ore ore : asteroid.getOreTypes()) {
                if (oreMap.containsKey(ore))
                    oreMap.put(ore, oreMap.get(ore) + 1);
                 else
                     oreMap.put(ore, 1);
            }

            for (Ore ore : oreMap.keySet()) {
                if (oreMap.get(ore) == 1)
                    lore.add("§6" + ore.toString());
                else
                    lore.add("§6" + ore.toString() + " §f" + oreMap.get(ore) + "x");
            }

            //Crete item and set pos key for teleporting when clicked
            ItemStack asteroidItem = createItemGUI("§f§lAsteroid #" + asteroid.getId(), lore, Material.FIRE_CHARGE);
            ItemMeta meta = asteroidItem.getItemMeta();
            if (meta != null) {
                meta.getPersistentDataContainer().set(Keys.positionKey, PersistentDataType.FLOAT, asteroid.getPositionX());
                meta.getPersistentDataContainer().set(Keys.radiusKey, PersistentDataType.INTEGER, asteroid.getRadius());
                asteroidItem.setItemMeta(meta);
            }

            upgradeGUI.setItem(guiIndex, asteroidItem);
            guiIndex++;

            if (guiIndex == 43)
                break;

            //TODO implement multiple pages
        }
        player.openInventory(upgradeGUI);
    }

    public void handleGUI(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() != null) {
           if (event.getCurrentItem().getType().equals(Material.GREEN_TERRACOTTA) || event.getCurrentItem().getType().equals(Material.RED_TERRACOTTA)) {
                    User user = getUserFromPlayer(player, Prison.connectedPlayers);
                    assert user != null;
                    toggleActive();
                    openGUI(player, 1);
           }
           else if (event.getCurrentItem().getType().equals(Material.FIRE_CHARGE)) {
               ItemMeta meta = event.getCurrentItem().getItemMeta();
               if (meta != null) {
                   if (meta.getPersistentDataContainer().has(Keys.positionKey, PersistentDataType.FLOAT)) {
                       float asteroidCenterX = meta.getPersistentDataContainer().get(Keys.positionKey, PersistentDataType.FLOAT);
                       int asteroidRadius = meta.getPersistentDataContainer().get(Keys.radiusKey, PersistentDataType.INTEGER);
                       //Teleport player to asteroid and setup bounds restrictions
                       player.teleport(new Location(getWorldByName(Prison.asteroidWorldName), asteroidCenterX + asteroidRadius + 10, 70 + asteroidRadius + 1, 4));
                       WorldBorder br = new net.minecraft.world.level.border.WorldBorder();
                       br.world = ((CraftWorld) player.getWorld()).getHandle();
                       br.setCenter(asteroidCenterX, 0);
                       br.setSize(200);
                       Utils.sendPacket(new ClientboundInitializeBorderPacket(br), player);
                       Utils.getUserFromPlayer(player, Prison.connectedPlayers).restrict(new Location(player.getWorld(), asteroidCenterX, 70, 0), 200);
                   }
               }
               else if (event.getCurrentItem().getType().equals(Material.ARROW)) {
                   openGUI(player, 1);
                   //TODO FINISH IMPLEMEnTING MULTIPLE PAGES
               }
           }
        }
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
