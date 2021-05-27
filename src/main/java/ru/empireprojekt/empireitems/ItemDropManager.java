package ru.empireprojekt.empireitems;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import ru.empireprojekt.empireitems.events.Drop;
import ru.empireprojekt.empireitems.files.DataManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemDropManager {
    public HashMap<String, Drop[]> mobDrops;
    public HashMap<String, Drop[]> blockDrops;
    DataManager dropsConfig;
    EmpireItems plugin;
    public DataManager getDropsConfig() {
        return dropsConfig;
    }
    public HashMap<String, Drop[]> getMobDrops(){
        return  mobDrops;
    }
    public HashMap<String, Drop[]> getBlocksDrops(){
        return blockDrops;
    }
    public ItemDropManager(EmpireItems plugin) {
        this.plugin = plugin;

        mobDrops = new HashMap<>();
        blockDrops = new HashMap<>();
        dropsConfig = new DataManager("drops.yml", plugin);
    }
    //Создание дропа предметов drops.yml <String=Name of Entity,Drop=drop>
    public void getDrop(HashMap<String, Drop[]> map, ConfigurationSection section) {
        for (String key_ : section.getKeys(false)) {
            ConfigurationSection sect = section.getConfigurationSection(key_);
            List<Drop> mDrops = new ArrayList<Drop>();
            if (sect != null && !sect.contains("entity")) {
                System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.YELLOW + key_ + " Не содержит entity!");
            }
            for (String itemKey : sect.getConfigurationSection("items").getKeys(false)) {
                ConfigurationSection mItem = sect.getConfigurationSection("items." + itemKey);
                if (mItem != null && mItem.contains("item"))
                    mDrops.add(new Drop(
                            mItem.getString("item"),
                            mItem.getInt("min_amount", 0),
                            mItem.getInt("max_amount", 0),
                            mItem.getDouble("chance", 0.0)
                    ));
                else
                    System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.YELLOW + key_ + " Не содержит выпадаемого предмета");
            }
            Drop[] drop = new Drop[0];
            drop = mDrops.toArray(drop);
            map.put(sect.getString("entity"), drop);
        }
    }
}
