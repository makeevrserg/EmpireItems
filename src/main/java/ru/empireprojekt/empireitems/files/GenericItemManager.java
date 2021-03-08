package ru.empireprojekt.empireitems.files;

import com.google.common.io.Files;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.empireprojekt.empireitems.EmpireItems;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenericItemManager {
    private EmpireItems plugin;
    private List<File> itemFilesList = null;
    private List<FileConfiguration> itemFilesConfigsList = null;
    public GenericItemManager(EmpireItems plugin) {
        this.plugin = plugin;
    }

    public void reloadConfig() {
        if (this.itemFilesList == null)
            LoadItemFiles();
        itemFilesConfigsList = new ArrayList<>();
        for (File f : itemFilesList) {
            this.itemFilesConfigsList.add(
                    YamlConfiguration.loadConfiguration(f)
            );
        }
    }
    public List<FileConfiguration> getConfig() {
        if (this.itemFilesConfigsList == null)
            reloadConfig();
        return this.itemFilesConfigsList;
    }
    private void LoadItemFiles() {
        itemFilesList = new ArrayList<>();

        if (this.plugin.getDataFolder().listFiles() != null) {
            File[] files = (new File(this.plugin.getDataFolder()+ File.separator +"items"+File.separator)).listFiles();
            if (files == null) {
                System.out.println(plugin.CONSTANTS.PLUGIN_MESSAGE + ChatColor.RED + "Нет файлов в папке");
                return;
            }
            Arrays.sort(files);

            for (final File fileEntry : files)
                if ((Files.getFileExtension(String.valueOf(fileEntry))).equalsIgnoreCase("yml")) {
                    System.out.println(ChatColor.GREEN + "Загрузка файла: " + this.plugin.getDataFolder() + File.separator +"items"+File.separator+ fileEntry.getName());
                    itemFilesList.add(
                            new File(this.plugin.getDataFolder() + File.separator +"items"+File.separator+ fileEntry.getName())
                    );
                }
        }
    }

}
