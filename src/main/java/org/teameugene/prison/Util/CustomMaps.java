package org.teameugene.prison.Util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.teameugene.prison.Prison;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.teameugene.prison.Util.Utils.*;

public class CustomMaps {
    private static Map<String, BufferedImage> images = new HashMap<String, BufferedImage>();
    private static final Map<Integer, String> savedImages = new HashMap<Integer, String>();
    private static final Plugin plugin = Prison.getInstance();
    private static final CustomFile dataFile = new CustomFile("data.yml");

    public static void Initialize() throws FileNotFoundException, Execeptions.DirectoryNotFoundException {
        boolean allMapsLoaded = true;
        File mapsDirectory = new File(plugin.getDataFolder(), "/maps/");
        if (mapsDirectory.exists() && mapsDirectory.isDirectory()) {
            String[] mapNames = mapsDirectory.list();
            if (mapNames != null) {
                for (String mapName : mapNames) {
                    File imageFile = new File(plugin.getDataFolder(), "/maps/" + mapName);
                    if (!imageFile.exists()) {
                        throw new FileNotFoundException("File not found: " + imageFile.getPath());
                    }
                    BufferedImage image = load(imageFile);
                    if (image != null) {
                        mapName = getSubstringBeforeCharacter(mapName, '.');
                        images.put(mapName, image);
                    } else {
                        plugin.getLogger().info("[Error]: Failed to load map " + mapName);
                        allMapsLoaded = false;
                    }
                }
                if (allMapsLoaded)
                    plugin.getLogger().info("[INFO]: All maps successfully loaded!");
            }
        } else {
            throw new Execeptions.DirectoryNotFoundException("Maps directory not found: " + mapsDirectory.getPath());
        }

        loadImages();
    }

    public static BufferedImage getMapImage(String mapName) {
        return images.get(mapName);
    }

    public static BufferedImage load(File imageFile) {
        BufferedImage image;
        try {
            image = ImageIO.read(imageFile);
            image = MapPalette.resizeImage(image);
        } catch (IOException e) {
            return null;
        }
        return image;
    }

    public static boolean hasImage(int id) {
        return savedImages.containsKey(id);
    }

    public static void saveImage(Integer id, String name) {
        getData().set("ids." + id, name);
        saveData();
    }

    public static BufferedImage getImage(int id) {
        return images.get(savedImages.get(id));
    }

    public static int getID(String mapName) {
        Integer obj = getKeyByValue(savedImages, mapName);
        return Objects.requireNonNullElse(obj, 0);
    }

    private static void loadImages() {
        if (getData().contains("ids"))
            getData().getConfigurationSection("ids").getKeys(false).forEach(id -> {
                savedImages.put(Integer.parseInt(id), getData().getString("ids." + id));
            });
    }

    public static FileConfiguration getData() {
        return dataFile.getConfig();
    }

    public static void saveData() {
        dataFile.saveConfig();
    }

    public static String getMapNameFromId(int id) {
        return savedImages.get(id);
    }

    public static ItemStack getMapItem(String mapName, World world) {
        int mapID = CustomMaps.getID(mapName);
        if (!CustomMaps.hasImage(mapID)) {
            MapView view = Bukkit.createMap(world);
            view.getRenderers().clear();
            BufferedImage mapImage = CustomMaps.getMapImage(mapName);
            if (mapImage == null) {
                return null;
            }

            ImageRenderer imageRenderer = new ImageRenderer(mapImage);
            view.addRenderer(imageRenderer);

            ItemStack map = new ItemStack(Material.FILLED_MAP);
            MapMeta meta = (MapMeta) map.getItemMeta();
            assert meta != null;
            meta.setMapView(view);
            map.setItemMeta(meta);
            saveImage(view.getId(), mapName);
            return map;
        } else {
            // Retrieve the existing map associated with the ID
            MapView existingMapView = Bukkit.getMap(mapID);
            if (existingMapView != null) {
                // Add the existing map to the player's inventory
                ItemStack map = new ItemStack(Material.FILLED_MAP);
                MapMeta meta = (MapMeta) map.getItemMeta();
                assert meta != null;
                meta.setMapView(existingMapView);
                map.setItemMeta(meta);
                return map;

            } else {
                Prison.getInstance().getLogger().info("[Error]: Map ID (" + mapID + ") exists but the map could not be retrieved.");
                return null;
            }
        }
    }
}