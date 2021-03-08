package ru.empireprojekt.empireitems.files;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.empireprojekt.empireitems.EmpireItems;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DataManager {
    private EmpireItems plugin;
    private String configName = "config.yml";
    private File configFiles = null;
    private FileConfiguration dataConfig = null;

    public DataManager(String configName, EmpireItems plugin) {
        this.configName = configName;
        this.plugin = plugin;
        saveDefaultConfig();
    }

    public void reloadConfig() {
        if (this.configFiles == null)
            this.configFiles = new File(this.plugin.getDataFolder(), configName);
        dataConfig = YamlConfiguration.loadConfiguration(configFiles);
        InputStream defaultStream = this.plugin.getResource(configName);
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            this.dataConfig.setDefaults(defaultConfig);
        }
    }

    public String getName(){
        return configName;
    }


    public FileConfiguration getConfig() {
        if (this.dataConfig == null)
            reloadConfig();
        return this.dataConfig;
    }

    public void LoadFiles() {
        configFiles = new File(this.plugin.getDataFolder(), configName);
    }

    public void updateConfig(FileConfiguration conf) {
        this.dataConfig = conf;
    }

    public void saveConfig() {
        if (this.configFiles == null || this.dataConfig == null)
            return;
        try {
            this.getConfig().save(this.configFiles);
        } catch (IOException e) {
            System.out.println("[EmpireTrading]" + ChatColor.RED + "Не удалось сохранить файл " + configName);
        }
    }
    public void saveDefaultConfig() {
        if (this.configFiles == null)
            this.configFiles = new File(this.plugin.getDataFolder(), configName);
        if (!this.configFiles.exists())
            this.plugin.saveResource(configName, false);
    }
}
