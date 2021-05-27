package ru.empireprojekt.empireitems;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemUpgradeManager {
    public HashMap<String, List<itemUpgradeClass>> itemUpgradesInfo;
    EmpireItems plugin;
    public HashMap<String, Double> itemUpgradeCostDecreaser;
    public HashMap<String, Double> getItemUpgradeCostDecreaser(){
        return  itemUpgradeCostDecreaser;
    }
    public ItemUpgradeManager(EmpireItems plugin){
        this.plugin = plugin;
        itemUpgradesInfo = new HashMap<>();
        itemUpgradeCostDecreaser = new HashMap<>();
    }
    public static class itemUpgradeClass {
        public String attribute;
        public String slot;
        public double max_add;
        public double min_add;
    }
    public void AddUpgradeDecreaser(String key,Double map){
        itemUpgradeCostDecreaser.put(key,map);
    }
    public void AddUpgrade(String key,List<itemUpgradeClass> list){
        itemUpgradesInfo.put(key,list);
    }
    public List<itemUpgradeClass> GetUpgrades(ConfigurationSection generic_item) {
        List<itemUpgradeClass> mUpgrades = new ArrayList<>();
        for (String itemAttribyte : generic_item.getConfigurationSection("upgrade").getKeys(false)) {
            itemUpgradeClass upgrade = new itemUpgradeClass();
            upgrade.attribute = itemAttribyte;
            upgrade.max_add = generic_item.getConfigurationSection("upgrade").getConfigurationSection(itemAttribyte).getDouble("max_add", 0.0);
            upgrade.min_add = generic_item.getConfigurationSection("upgrade").getConfigurationSection(itemAttribyte).getDouble("min_add", 0.0);
            upgrade.slot = generic_item.getConfigurationSection("upgrade").getConfigurationSection(itemAttribyte).getString("slot", "HAND");
            mUpgrades.add(upgrade);
        }
        return mUpgrades;
    }
}
