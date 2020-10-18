package ru.empireprojekt.empireitems;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import ru.empireprojekt.empireitems.ItemManager.GenericItem;
import ru.empireprojekt.empireitems.events.GenericListener;
import ru.empireprojekt.empireitems.events.InteractEvent;
import ru.empireprojekt.empireitems.files.GenericItemManager;

import java.util.*;

public class EmpireItems extends JavaPlugin {
    public GenericItemManager generic_item;

    @Override
    public void onEnable() {
        System.out.println("-----------------------------------------------------------");
        System.out.println("Plugin EmpireItems has been Enabled!");
        System.out.println("-----------------------------------------------------------");
        LoadGenericItems();
        getServer().getPluginManager().registerEvents(new GenericListener(item_events),this);
    }

    private void LoadGenericItems() {
        this.generic_item = new GenericItemManager(this);
        FileConfiguration file_generic_item = generic_item.getConfig();

        ConfigurationSection crafting_table = file_generic_item.getConfigurationSection("crafting_table");

        //Listing items under crafting_table
        for (String key : crafting_table.getKeys(false)) {
            System.out.println(key);
            ConfigurationSection generic_item = crafting_table.getConfigurationSection(key);
            GenericItem genericItem = new GenericItem();
            if (!generic_item.contains("display_name")
                    || !generic_item.contains("lore")
                    || !generic_item.contains("permission")
                    || !generic_item.contains("enabled")
                    || !generic_item.contains("custom_model_data")
                    || (!generic_item.contains("texture_path") && !generic_item.contains("model_path"))
            ) {
                System.out.println(ChatColor.RED + "Предмет " + key + " не имеет необходимых значений:.");
                System.out.println(ChatColor.RED + "display_name lore permission enabled custom_model_data (texture_path or model_path)");
                continue;
            }

            genericItem.display_name = generic_item.getString("display_name");
            genericItem.lore = generic_item.getStringList("lore");
            genericItem.permission = generic_item.getString("permission");
            genericItem.enabled = generic_item.getBoolean("enabled");
            genericItem.customModelData = generic_item.getInt("custom_model_data");


            if (generic_item.contains("texture_path") && generic_item.contains("model_path"))
                System.out.println(ChatColor.YELLOW + "Вы используете model_path и texture_path в " + key + ". Необходимо выбрать что-то одно.");
            genericItem.texture_path = generic_item.getString("texture_path");
            genericItem.model_path = generic_item.getString("model_path");
            if (generic_item.contains("enchantements")) {

                genericItem.enchantements = new HashMap<String, Integer>();
                for (String ing_key : generic_item.getConfigurationSection("enchantements").getKeys(false))
                    genericItem.enchantements.put(ing_key, generic_item.getConfigurationSection("enchantements").getInt(ing_key));


            }

            if (generic_item.contains("pattern") && generic_item.contains("ingredients") && generic_item.contains("amount")) {
                genericItem.amount = generic_item.getInt("amount");
                genericItem.pattern = generic_item.getList("pattern");
                genericItem.ingredients = new HashMap<Character, String>();
                for (String ing_key : generic_item.getConfigurationSection("ingredients").getKeys(false))
                    genericItem.ingredients.put(ing_key.charAt(0), generic_item.getConfigurationSection("ingredients").getString(ing_key));
                genericItem.PrintItem();
                System.out.println(
                        genericItem.ingredients.keySet());
            }
            CreateItem(genericItem,key);
        }
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("empireitems")) {
            if (!sender.hasPermission("empireitems.reload")) {
                sender.sendMessage(ChatColor.RED + "У вас нет разрешения использовать эту команду");
                return true;
            }
            if (args.length == 0) {
                //todo
            }
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("reload")) {
                    sender.sendMessage(ChatColor.GREEN + "Перезагружаем EmpireItems");
                    items = new HashMap<String, ItemStack>();
                    item_events =new HashMap<ItemMeta, InteractEvent>();
                    Bukkit.clearRecipes();
                    this.reloadConfig();
                    LoadGenericItems();
                    sender.sendMessage(ChatColor.GREEN + "Плагин успешно перезагружен!");
                    return true;
                }
                if (args[0].equalsIgnoreCase("give") && args[1]!=null){
                    Player player =(Player)sender;
                    player.getInventory().addItem(items.get(args[1]));
                    //todo
                }
            }
        }
        return false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public Map<String,ItemStack> items = new HashMap<String, ItemStack>();
    public Map<ItemMeta, InteractEvent> item_events =new HashMap<ItemMeta, InteractEvent>();

    private ItemStack CreateItem(GenericItem genericItem,String key){
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(genericItem.display_name);
        meta.setLore(genericItem.lore);
        meta.setCustomModelData(genericItem.customModelData);

        if (genericItem.enchantements!=null){
            for (String keys: genericItem.enchantements.keySet()){
                if (Enchantment.getByKey(NamespacedKey.minecraft(keys.toLowerCase())) == null)
                    System.out.println(ChatColor.RED+" Введено неверное зачарование "+ keys +". Используйте зачарования из minecraft, а не spigot!");
                else
                    meta.addEnchant(Enchantment.getByKey(NamespacedKey.minecraft(keys.toLowerCase())),genericItem.enchantements.get(keys),true);
            }
        }
        item.setItemMeta(meta);
        InteractEvent event = new InteractEvent();
        event.click="RIGHT_CLICK_AIR";
        event.play_sound="minecraft:entity.creeper.hurt";
        item_events.put(meta,event);
        items.put(key,item);
        if (genericItem.pattern != null)
            Bukkit.addRecipe(getGenericRecipe(genericItem,item));
        return item;
    }

    private ShapedRecipe getGenericRecipe(GenericItem genericItem,ItemStack item) {


        NamespacedKey key = new NamespacedKey(this, "stick" + genericItem.permission);
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        if (genericItem.pattern != null) {
            System.out.println(String.valueOf(genericItem.pattern.get(0)));
            System.out.println(genericItem.pattern.get(0).toString());
            recipe.shape(genericItem.pattern.get(0).toString(), genericItem.pattern.get(1).toString(), genericItem.pattern.get(2).toString());

            for (char keys : genericItem.ingredients.keySet())
                recipe.setIngredient(keys, Material.getMaterial(genericItem.ingredients.get(keys)));
        }
        return recipe;
    }


}
