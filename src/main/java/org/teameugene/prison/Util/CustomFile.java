package org.teameugene.prison.Util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.teameugene.prison.Prison;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

class CustomFile {

    private final Prison plugin = Prison.getInstance();
    private FileConfiguration dataConfig = null;
    private File dataConfigFile = null;
    private final String name;

    public CustomFile(String name) {
        this.name = name;
        saveDefaultConfig();
    }

    public void reloadConfig() {
        if (dataConfigFile == null)
            dataConfigFile = new File(plugin.getDataFolder(),name);

        this.dataConfig = YamlConfiguration
                .loadConfiguration(dataConfigFile);

        InputStream defConfigStream = plugin.getResource(name);
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration
                    .loadConfiguration(new InputStreamReader(defConfigStream));
            this.dataConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getConfig() {
        if (this.dataConfig == null)
            reloadConfig();
        return this.dataConfig;
    }

    public void saveConfig() {
        if ((dataConfig == null) || (dataConfigFile == null))
            return;
        try {
            getConfig().save(dataConfigFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to "
                    + dataConfigFile, e);
        }
    }

    public void saveDefaultConfig() {
        if (dataConfigFile == null)
            dataConfigFile = new File(plugin.getDataFolder(), name);
        if (!dataConfigFile.exists())
            plugin.saveResource(name, false);
    }

}
