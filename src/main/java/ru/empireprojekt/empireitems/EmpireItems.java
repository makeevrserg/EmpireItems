package ru.empireprojekt.empireitems;

import com.google.common.collect.Multimap;
import io.github.bananapuncher714.nbteditor.NBTEditor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.Attr;
import ru.empireprojekt.empireitems.ItemManager.GenericItem;
import ru.empireprojekt.empireitems.ItemManager.mAttribute;
import ru.empireprojekt.empireitems.ItemManager.mPotionEffect;
import ru.empireprojekt.empireitems.events.GenericListener;
import ru.empireprojekt.empireitems.events.InteractEvent;
import ru.empireprojekt.empireitems.files.GenericItemManager;

import java.util.*;

public class EmpireItems extends JavaPlugin {
    public GenericItemManager generic_item;
    public Map<String, ItemStack> items = new HashMap<String, ItemStack>();
    public Map<ItemMeta, List<InteractEvent>> item_events = new HashMap<ItemMeta, List<InteractEvent>>();
    GenericListener genericListener;

    @Override
    public void onEnable() {
        System.out.println("-----------------------------------------------------------");
        System.out.println("Plugin EmpireItems has been Enabled!");
        System.out.println("-----------------------------------------------------------");
        LoadGenericItems();
        genericListener = new GenericListener(item_events);
        getServer().getPluginManager().registerEvents(genericListener, this);
    }

    private void LoadGenericItems() {
        this.generic_item = new GenericItemManager(this);
        List<FileConfiguration> file_generic_items = generic_item.getConfig();
        for (FileConfiguration file_generic_item:file_generic_items) {
            ConfigurationSection crafting_table = file_generic_item.getConfigurationSection("crafting_table");

            //Listing items under crafting_table
            if (crafting_table==null) {
                System.out.println(ChatColor.RED + "В файле" + file_generic_item.getName() + " отсутствует поле crafting_table. Невозможно загрухить файл.");
                continue;
            }
            for (String key : crafting_table.getKeys(false)) {
                System.out.println(key);
                ConfigurationSection generic_item = crafting_table.getConfigurationSection(key);
                GenericItem genericItem = new GenericItem();
                assert generic_item != null;
                if (!generic_item.contains("display_name")
                        || !generic_item.contains("lore")
                        || !generic_item.contains("permission")
                        || !generic_item.contains("enabled")
                        || !generic_item.contains("custom_model_data")
                        || !generic_item.contains("material")
                        || (!generic_item.contains("texture_path") && !generic_item.contains("model_path"))
                ) {
                    System.out.println(ChatColor.RED + "Предмет " + key + " не имеет одного из необходимых значений:");
                    System.out.println(ChatColor.RED + "display_name lore permission enabled custom_model_data material (texture_path or model_path)");
                    continue;
                }
                genericItem.display_name = generic_item.getString("display_name");
                genericItem.lore = generic_item.getStringList("lore");
                genericItem.permission = generic_item.getString("permission");
                genericItem.material = generic_item.getString("material");
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
                genericItem.attributes = new ArrayList<mAttribute>();
                if (generic_item.contains("attributes")){
                    for (String attr_key: generic_item.getConfigurationSection("attributes").getKeys(false)){
                        System.out.println("attributes."+attr_key+".name");
                        if (generic_item.contains("attributes."+attr_key+".name"))
                            genericItem.attributes.add(
                                    new mAttribute(
                                            generic_item.getString("attributes." + attr_key + ".name"),
                                            generic_item.getDouble("attributes." + attr_key + ".amount", 0.0),
                                            generic_item.getString("attributes." + attr_key + ".equipment_slot","HAND")
                                    )
                            );
                    }
                }

                if (generic_item.contains("interact")) {
                    genericItem.events = new ArrayList<InteractEvent>();
                    for (String event_key : generic_item.getConfigurationSection("interact").getKeys(false)) {
                        if (event_key.equals("RIGHT_CLICK")
                                || event_key.equals("LEFT_CLICK")
                        ) {
                            ConfigurationSection event = generic_item.getConfigurationSection("interact").getConfigurationSection(event_key);
                            InteractEvent interactEvent = new InteractEvent();
                            interactEvent.click = event_key;
                            interactEvent.play_particle = event.getString("play_particle.name", null);
                            interactEvent.particle_count = event.getInt("play_particle.count", 100);
                            interactEvent.as_console = event.getBoolean("as_console", false);
                            interactEvent.particle_time = event.getDouble("play_particle.time", 0.2);
                            interactEvent.play_sound = event.getString("play_sound.name", null);
                            interactEvent.sound_volume = event.getInt("play_sound.volume", 1);
                            interactEvent.sound_pitch = event.getInt("play_sound.pitch", 1);
                            interactEvent.increment_durability = event.getInt("increment_durability", 0);
                            interactEvent.decrement_durability = event.getInt("decrement_durability", 0);
                            interactEvent.execute_commands = event.getStringList("execute_commands");
                            interactEvent.remove_potion_effect = event.getStringList("remove_potion_effect");
                            if (event.contains("potion_effect"))
                                for (String potion_key : event.getConfigurationSection("potion_effect").getKeys(false)) {
                                    mPotionEffect potionEffect = new mPotionEffect();
                                    potionEffect.name = potion_key;
                                    potionEffect.amplifier = event.getConfigurationSection("potion_effect").getConfigurationSection(potion_key).getInt("amplifier", 1);
                                    potionEffect.duration = event.getConfigurationSection("potion_effect").getConfigurationSection(potion_key).getInt("duration", 1);
                                }

                            genericItem.events.add(interactEvent);
                            System.out.println("Added event " + event.getString("play_effect.name", null));
                        } else {
                            System.out.println(ChatColor.YELLOW + "Введен неподдерживаемый эвент " + event_key);
                        }
                    }
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
                CreateItem(genericItem, key);
            }
        }
    }


    private boolean reload(CommandSender sender){
        if (!sender.hasPermission("empireitems.reload")) {
            sender.sendMessage(ChatColor.RED + "У вас нет разрешения использовать эту команду");
            return true;
        }
        sender.sendMessage(ChatColor.GREEN + "Перезагружаем EmpireItems");
        items = new HashMap<String, ItemStack>();
        item_events = new HashMap<ItemMeta, List<InteractEvent>>();
        Bukkit.clearRecipes();
        this.reloadConfig();
        LoadGenericItems();
        genericListener.ReloadListener(item_events);
        sender.sendMessage(ChatColor.GREEN + "Плагин успешно перезагружен!");
        return true;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (label.equalsIgnoreCase("ereload"))
            return reload(sender);

        if (label.equalsIgnoreCase("empireitems")||label.equalsIgnoreCase("emp")) {


            if (args.length == 0) {
                //todo
            }
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("reload"))
                    return reload(sender);

                if (args[0].equalsIgnoreCase("give") && args[1] != null) {
                    Player player = (Player) sender;
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


    private EquipmentSlot getEquipementSlot(String slot){
        if (slot.equalsIgnoreCase("HAND"))
            return EquipmentSlot.HAND;
        if (slot.equalsIgnoreCase("CHEST"))
            return EquipmentSlot.CHEST;
        if (slot.equalsIgnoreCase("FEET"))
            return EquipmentSlot.FEET;
        if (slot.equalsIgnoreCase("HEAD"))
            return EquipmentSlot.HEAD;
        if (slot.equalsIgnoreCase("LEGS"))
            return EquipmentSlot.LEGS;
        if (slot.equalsIgnoreCase("OFF_HAND"))
            return EquipmentSlot.OFF_HAND;
        return null;
    }
    private Attribute getAttribute(String attribute){
        if (attribute.equalsIgnoreCase("GENERIC_ARMOR"))
            return Attribute.GENERIC_ARMOR;

        if (attribute.equalsIgnoreCase("GENERIC_ARMOR_TOUGHNESS"))
            return Attribute.GENERIC_ARMOR_TOUGHNESS;
        if (attribute.equalsIgnoreCase("GENERIC_ATTACK_DAMAGE"))
            return Attribute.GENERIC_ATTACK_DAMAGE;
        if (attribute.equalsIgnoreCase("GENERIC_ATTACK_KNOCKBACK"))
            return Attribute.GENERIC_ATTACK_KNOCKBACK;
        if (attribute.equalsIgnoreCase("GENERIC_ATTACK_SPEED"))
            return Attribute.GENERIC_ATTACK_SPEED;
        if (attribute.equalsIgnoreCase("GENERIC_FOLLOW_RANGE"))
            return Attribute.GENERIC_FOLLOW_RANGE;
        if (attribute.equalsIgnoreCase("GENERIC_KNOCKBACK_RESISTANCE"))
            return Attribute.GENERIC_KNOCKBACK_RESISTANCE;
        if (attribute.equalsIgnoreCase("GENERIC_LUCK"))
            return Attribute.GENERIC_LUCK;
        if (attribute.equalsIgnoreCase("GENERIC_MAX_HEALTH"))
            return Attribute.GENERIC_MAX_HEALTH;
        if (attribute.equalsIgnoreCase("GENERIC_MOVEMENT_SPEED"))
            return Attribute.GENERIC_MOVEMENT_SPEED;
        if (attribute.equalsIgnoreCase("HORSE_JUMP_STRENGTH"))
            return Attribute.HORSE_JUMP_STRENGTH;
        return null;
    }
    private ItemStack CreateItem(GenericItem genericItem, String key) {

        ItemStack item = new ItemStack(Material.getMaterial(genericItem.material));
        ItemMeta meta = item.getItemMeta();

        for (mAttribute attr:genericItem.attributes){
            meta.addAttributeModifier(
              getAttribute(attr.name),
              new AttributeModifier(
                      UUID.randomUUID(),
                      getAttribute(attr.name).name(),
                      attr.amount,
                      AttributeModifier.Operation.ADD_NUMBER,
                      getEquipementSlot(attr.equipment_slot)
              )
            );
        }
//        //https://minecraft.gamepedia.com/Attribute
//        meta.addAttributeModifier(
//                Attribute.GENERIC_MAX_HEALTH,
//                new AttributeModifier(UUID.randomUUID(),Attribute.GENERIC_MAX_HEALTH.name(),100,AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND)
//        );

        meta.setDisplayName(genericItem.display_name);
        meta.setLore(genericItem.lore);
        meta.setCustomModelData(genericItem.customModelData);
        if (genericItem.enchantements != null) {
            for (String keys : genericItem.enchantements.keySet()) {
                if (Enchantment.getByKey(NamespacedKey.minecraft(keys.toLowerCase())) == null)
                    System.out.println(ChatColor.RED + " Введено неверное зачарование " + keys + ". Используйте зачарования из minecraft, а не spigot!");
                else
                    meta.addEnchant(Enchantment.getByKey(NamespacedKey.minecraft(keys.toLowerCase())), genericItem.enchantements.get(keys), true);
            }
        }

        if (genericItem.events != null) {
            item_events.put(meta, genericItem.events);
        }
        item.setItemMeta(meta);

        items.put(key, item);
        if (genericItem.pattern != null)
            Bukkit.addRecipe(getGenericRecipe(genericItem, item));
        return item;
    }

    private ShapedRecipe getGenericRecipe(GenericItem genericItem, ItemStack item) {


        NamespacedKey key = new NamespacedKey(this, genericItem.material + genericItem.permission);
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
