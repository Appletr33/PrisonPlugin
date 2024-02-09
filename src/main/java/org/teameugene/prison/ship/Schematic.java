package org.teameugene.prison.ship;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class Schematic {

    private final Clipboard clipboard;
    private String schematicName;

    public Schematic(Clipboard clipboard) {
        this.clipboard = clipboard;
    }

    public Clipboard getClipboard() {
        return clipboard;
    }

    public void setName(String name) {
        this.schematicName = name;
    }

    public String getName() {
        return this.schematicName;
    }

    public static ArrayList<Schematic> loadSchematics(Plugin plugin) {
        File schematicFolder = new File(plugin.getDataFolder() + "/../WorldEdit/schematics/");
        ArrayList<Schematic> schematics = new ArrayList<>();

        // Check if the directory exists
        if (schematicFolder.exists() && schematicFolder.isDirectory()) {
            File[] files = schematicFolder.listFiles();

            if (files != null) {
                for (File file : files) {
                    // Check if the file is a regular file (not a directory)
                    if (file.isFile() && file.getName().endsWith(".schem")) {
                        // Do something with each file
                        String schematicName = file.getName();
                        plugin.getLogger().info("Found schematic file: " + schematicName);
                        Optional<Schematic> schematic = load(file);
                        if (schematic.isPresent()) {
                            schematic.get().setName(schematicName);
                            schematics.add(schematic.get());
                        }
                    }
                }
            } else {
                // Handle the case where listing files failed
                plugin.getLogger().warning("Failed to list files in the schematic folder");
            }
        } else {
            // Handle the case where the schematic folder does not exist
            plugin.getLogger().warning("Schematic folder does not exist: " + schematicFolder.getAbsolutePath());
        }

        return schematics;
    }

    public void paste(org.bukkit.Location target) {
        World world = BukkitAdapter.adapt(target.getWorld());
        Location location = BukkitAdapter.adapt(target);

        EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);

        Operation operation = new ClipboardHolder(clipboard).createPaste(session)
                .to(location.toVector().toBlockPoint()).ignoreAirBlocks(true).build();

        try {
            Operations.complete(operation);

            session.flushSession();
        } catch (WorldEditException exception) {
            exception.printStackTrace();
        }
    }

    public static Optional<Schematic> load(File file) {
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            return Optional.empty();
        }

        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            return Optional.of(new Schematic(reader.read()));
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return Optional.empty();
    }
}
