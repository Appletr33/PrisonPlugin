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

import static org.teameugene.prison.Util.CustomMaps.getMapItem;

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
        ItemStack map = getMapItem(args[0], player.getWorld());
        if (map != null) {
            player.getInventory().addItem(map);
            player.sendMessage("Map Created!");
        } else {
            player.sendMessage("Image [" + args[0] + "]" + " could not be loaded!");
            return false;
        }
        return true;
    }
}
