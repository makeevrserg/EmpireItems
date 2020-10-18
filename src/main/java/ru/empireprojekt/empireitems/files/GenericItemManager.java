package ru.empireprojekt.empireitems.files;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.empireprojekt.empireitems.EmpireItems;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class GenericItemManager {
    private EmpireItems plugin;
    private FileConfiguration genericItemConfig = null;
    private File configFile = null;

    public GenericItemManager(EmpireItems plugin) {
        this.plugin = plugin;
        //initialize config file
        saveDefaultConfig();
    }

    public void reloadConfig() {
        if (this.configFile == null)
            this.configFile = new File(this.plugin.getDataFolder(), "generic_item.yml");
        this.genericItemConfig = YamlConfiguration.loadConfiguration(this.configFile);
        InputStream defaultStream = this.plugin.getResource("generic_item.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            this.genericItemConfig.setDefaults(defaultConfig);
        }
    }

    public FileConfiguration getConfig() {
        if (this.genericItemConfig == null)
            reloadConfig();
        return this.genericItemConfig;
    }

    public void saveConfig() {
        if (this.genericItemConfig == null || this.configFile == null)
            return;
        try {
            this.getConfig().save(this.configFile);
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.configFile, e);
        }
    }

    public void saveDefaultConfig() {
        if (this.configFile == null)
            this.configFile = new File(this.plugin.getDataFolder(), "generic_item.yml");
        if (!this.configFile.exists()) {
            this.plugin.saveResource("generic_item.yml", false);
        }
    }
}
