package ru.empireprojekt.empireitems.files;

import com.google.common.io.Files;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.empireprojekt.empireitems.EmpireItems;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GenericItemManager {
    private EmpireItems plugin;
    private List<File> itemFilesList = null;
    private List<FileConfiguration> itemFilesConfigsList = null;

    private File guiFile = null;
    private FileConfiguration guiFileConfig = null;


    public GenericItemManager(EmpireItems plugin) {
        this.plugin = plugin;
    }


    public void reloadConfig() {
        if (this.itemFilesList == null)
            LoadItemFiles();
        if (this.guiFile == null)
            this.guiFile = new File(this.plugin.getDataFolder(), "gui.yml");

        this.guiFileConfig = YamlConfiguration.loadConfiguration(this.guiFile);


        itemFilesConfigsList = new ArrayList<FileConfiguration>();
        for (File f : itemFilesList) {
            this.itemFilesConfigsList.add(
                    YamlConfiguration.loadConfiguration(f)
            );
        }
    }

    public FileConfiguration getGuiConfig(){
        if (this.guiFileConfig!=null)
            return this.guiFileConfig;
        else
            return null;
    }
    public List<FileConfiguration> getConfig() {
        if (this.itemFilesConfigsList == null)
            reloadConfig();
        return this.itemFilesConfigsList;
    }


    private void LoadItemFiles() {
        itemFilesList = new ArrayList<File>();

        if (this.plugin.getDataFolder().listFiles() != null)
            for (final File fileEntry : this.plugin.getDataFolder().listFiles())
                if ((Files.getFileExtension(String.valueOf(fileEntry))).equalsIgnoreCase("yml")) {
                    System.out.println(ChatColor.GREEN + "Загрузка файла: " + this.plugin.getDataFolder() +File.separator + fileEntry.getName());
                    itemFilesList.add(
                            new File(this.plugin.getDataFolder() + File.separator + fileEntry.getName())
                    );
                }
    }

}
