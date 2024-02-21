package org.teameugene.prison.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.teameugene.prison.Util.CustomMaps;
import org.teameugene.prison.Util.ImageRenderer;

import java.awt.image.BufferedImage;

public class MapCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return false;
        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("Wrong usage, /map <link>");
            return false;
        }

        int mapID = CustomMaps.getID(args[0]);
        if (!CustomMaps.hasImage(mapID)) {
            MapView view = Bukkit.createMap(player.getWorld());
            view.getRenderers().clear();
            BufferedImage mapImage = CustomMaps.getMapImage(args[0]);
            if (mapImage == null) {
                player.sendMessage("Image [" + args[0] + "]" + " Doesnt exist and could not be loaded!");
                return false;
            }

            ImageRenderer imageRenderer = new ImageRenderer(mapImage);
            view.addRenderer(imageRenderer);

            ItemStack map = new ItemStack(Material.FILLED_MAP);
            MapMeta meta = (MapMeta) map.getItemMeta();
            assert meta != null;
            meta.setMapView(view);
            map.setItemMeta(meta);

            player.getInventory().addItem(map);
            player.sendMessage("New Map Created!");

            CustomMaps.saveImage(view.getId(), args[0]);
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

                player.getInventory().addItem(map);
                player.sendMessage("Existing Map Retrieved!");
            } else {
                player.sendMessage("Error: Map ID exists but the map could not be retrieved.");
            }
        }
        return true;
    }
}
