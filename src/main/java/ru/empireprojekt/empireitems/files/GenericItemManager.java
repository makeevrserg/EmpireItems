package ru.empireprojekt.empireitems.files;

import com.google.common.io.Files;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.empireprojekt.empireitems.EmpireItems;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class GenericItemManager {
    private EmpireItems plugin;
    private List<File> configFiles = null;
    private List<FileConfiguration> itemFilesConfig = null;

    public GenericItemManager(EmpireItems plugin) {
        this.plugin = plugin;
        //initialize config file
        reloadConfig();
    }

    private void reloadConfig() {
        if (this.configFiles == null) {
            LoadFiles();
        }
        itemFilesConfig=new ArrayList<FileConfiguration>();
        for (File f: configFiles){
            this.itemFilesConfig.add(
                    YamlConfiguration.loadConfiguration(f)
            );
            InputStream defaultStream = this.plugin.getResource(f.getName());
            if (defaultStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
                this.itemFilesConfig.get(this.itemFilesConfig.size()-1).setDefaults(defaultConfig);
            }
        }
    }

    public List<FileConfiguration> getConfig() {
        if (this.itemFilesConfig == null)
            reloadConfig();
        return this.itemFilesConfig;
    }

    private void LoadFiles(){
        configFiles = new ArrayList<File>();
        if (this.plugin.getDataFolder().listFiles()!=null)
            for (final File fileEntry : this.plugin.getDataFolder().listFiles())
                if ((Files.getFileExtension(String.valueOf(fileEntry))).equalsIgnoreCase("yml")) {
                    configFiles.add(
                            new File(this.plugin.getDataFolder(), fileEntry.getName())
                    );
                }
        else
            System.out.println(ChatColor.RED+"В папке плагина не обнаружено файлов с предметами");
    }

}
