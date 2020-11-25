package ru.empireprojekt.empireitems;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.empireprojekt.empireitems.ItemManager.*;
import ru.empireprojekt.empireitems.ItemManager.menusystem.MenuItems;
import ru.empireprojekt.empireitems.ItemManager.menusystem.MenuListener;
import ru.empireprojekt.empireitems.ItemManager.menusystem.PlayerMenuUtility;
import ru.empireprojekt.empireitems.ItemManager.menusystem.menu.EmpireCategoriesMenu;
import ru.empireprojekt.empireitems.events.Drop;
import ru.empireprojekt.empireitems.events.GenericListener;
import ru.empireprojekt.empireitems.events.InteractEvent;
import ru.empireprojekt.empireitems.files.GenericItemManager;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmpireItems extends JavaPlugin {

    public GenericItemManager generic_item;
    public Map<String, ItemStack> items;
    public HashMap<ItemStack, ShapedRecipe> itemRecipe;
    public Map<ItemMeta, List<InteractEvent>> item_events;
    TabCompletition tabCompletition;
    GenericListener genericListener;
    ItemManager itemManager;
    public List<MenuItems> menuItems;

    public HashMap<String, List<Drop>> mobDrops;
    public HashMap<String, List<Drop>> blockDrops;
    private static final HashMap<Player, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<Player, PlayerMenuUtility>();

    public static PlayerMenuUtility getPlayerMenuUtility(Player p) {
        PlayerMenuUtility playerMenuUtility;
        if (playerMenuUtilityMap.containsKey(p))
            return playerMenuUtilityMap.get(p);
        else {
            playerMenuUtility = new PlayerMenuUtility(p);
            playerMenuUtilityMap.put(p, playerMenuUtility);
            return playerMenuUtility;
        }
    }



    private final Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
    public void HEXPattern(List<String> list){
        for(int i =0;i<list.size();++i)
            list.set(i,HEXPattern(list.get(i)));

    }
    public String HEXPattern(String line){
        Matcher match = pattern.matcher(line);
        while (match.find()){
            String color = line.substring(match.start(),match.end());
            line = line.replace(color, net.md_5.bungee.api.ChatColor.of(color)+"");
            match = pattern.matcher(line);
        }

        return ChatColor.translateAlternateColorCodes('&',line);
    }


    private void generateMenuItems() {
        menuItems = new ArrayList<MenuItems>();
        ConfigurationSection categories = generic_item.getGuiConfig().getConfigurationSection("categories");
        for (String key : categories.getKeys(false)) {
            ConfigurationSection sect = categories.getConfigurationSection(key);
            menuItems.add(
                    new MenuItems(sect.getString("name", "null"),
                            sect.getString("icon", "STONE"),
                            sect.getStringList("lore"),
                            sect.getString("permission", "null"),
                            sect.getStringList("items")
                    ));
        }
    }

    @Override
    public void onEnable() {
        System.out.println("-----------------------------------------------------------");
        System.out.println("Enabling EmpireItems...");
        EnableFunc();
        System.out.println("Plugin EmpireItems has been Enabled!");
        System.out.println("-----------------------------------------------------------");

    }
    private void EnableFunc(){
        itemManager = new ItemManager(this);
        item_events = new HashMap<ItemMeta, List<InteractEvent>>();
        items = new HashMap<String, ItemStack>();
        LoadGenericItems();
        genericListener = new GenericListener(this, item_events);
        getServer().getPluginManager().registerEvents(genericListener, this);
        tabCompletition = new TabCompletition(itemManager.GetNames());
        getCommand("emp").setTabCompleter(tabCompletition);
        getCommand("empireitems").setTabCompleter(tabCompletition);
        generateMenuItems();
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
    }

    private void getDrop(HashMap<String, List<Drop>> map,ConfigurationSection section) {
        for (String key_ : section.getKeys(false)) {
            ConfigurationSection sect = section.getConfigurationSection(key_);
            List<Drop> mDrops = new ArrayList<Drop>();
            for (String itemKey : sect.getConfigurationSection("items").getKeys(false)) {
                ConfigurationSection mItem = sect.getConfigurationSection("items." + itemKey);
                mDrops.add(new Drop(
                        mItem.getString("item"),
                        mItem.getInt("min_amount", 0),
                        mItem.getInt("max_amount", 0),
                        mItem.getDouble("chance", 0.0)
                ));
            }
            map.put(sect.getString("entity"), mDrops);
        }
    }




    private void LoadGenericItems() {
        itemRecipe = new HashMap<ItemStack, ShapedRecipe>();
        this.generic_item = new GenericItemManager(this);
        List<FileConfiguration> file_generic_items = generic_item.getConfig();
        for (FileConfiguration file_generic_item : file_generic_items) {
            boolean DEBUG_ITEM = false;
            ConfigurationSection yml_items = file_generic_item.getConfigurationSection("yml_items");
            //Listing yml_items under yml_items

            mobDrops = new HashMap<String, List<Drop>>();
            blockDrops = new HashMap<String, List<Drop>>();
            if (file_generic_item.getConfigurationSection("loot") != null) {
                ConfigurationSection loot = file_generic_item.getConfigurationSection("loot");
                if (loot.getConfigurationSection("mobs") != null) {
                    ConfigurationSection mobs = loot.getConfigurationSection("mobs");
                    getDrop(mobDrops,mobs);

                }
                if (loot.getConfigurationSection("blocks") != null){

                    ConfigurationSection blocks = loot.getConfigurationSection("blocks");
                    getDrop(blockDrops,blocks);
                }


            }
            if (yml_items == null) {
                System.out.println(ChatColor.RED + "В файле" + file_generic_item.getName() + " отсутствует поле yml_items.");
                continue;
            }
            for (String key : yml_items.getKeys(false)) {
                //System.out.println("Предмет: " + key);
                ConfigurationSection generic_item = yml_items.getConfigurationSection(key);
                GenericItem genericItem = new GenericItem();
                if (!generic_item.contains("display_name")
                        || !generic_item.contains("permission")
                        || !generic_item.contains("enabled")
                        || !generic_item.contains("material")
                        || !generic_item.contains("custom_model_data")
                        || (!generic_item.contains("texture_path") && !generic_item.contains("model_path"))
                ) {
                    System.out.println(ChatColor.RED + "Предмет " + key + " не имеет одного из необходимых значений:");
                    System.out.println(ChatColor.RED + "display_name permission enabled  material custom_model_data (texture_path or model_path)");
                    continue;
                }
                genericItem.display_name = HEXPattern(generic_item.getString("display_name"));
                genericItem.lore = generic_item.getStringList("lore");
                HEXPattern(genericItem.lore);
                genericItem.permission = generic_item.getString("permission");
                genericItem.material = generic_item.getString("material");
                genericItem.customModelData = generic_item.getInt("custom_model_data");
                genericItem.enabled = generic_item.getBoolean("enabled");
                genericItem.itemFlags = new ArrayList<String>();
                genericItem.itemFlags = generic_item.getStringList("item_flags");
                genericItem.durability = generic_item.getInt("durability", -1);
                DEBUG_ITEM = generic_item.getBoolean("debug", false);

                if (generic_item.contains("texture_path") && generic_item.contains("model_path")) {
                    System.out.println(ChatColor.YELLOW + "Вы используете model_path и texture_path в " + key + ". Необходимо выбрать что-то одно. Предмет не был добавлен");
                    continue;
                }
                genericItem.texture_path = generic_item.getString("texture_path");
                genericItem.model_path = generic_item.getString("model_path");

                if (generic_item.contains("ingredients") && generic_item.contains("pattern")) {
                    genericItem.ingredients = new HashMap<Character, String>();
                    genericItem.pattern = generic_item.getList("pattern");
                    for (String k : generic_item.getConfigurationSection("ingredients").getKeys(false)) {
                        genericItem.ingredients.put(k.charAt(0), generic_item.getConfigurationSection("ingredients").getString(k));
                    }
                }

                //Добавляем предмет в json файла ресурс-пака
                itemManager.AddItem(file_generic_item.getString("namespace", "empire_items"), genericItem.texture_path, genericItem.model_path, key, genericItem.material, genericItem.customModelData);

                //Зачарования
                if (generic_item.contains("enchantements")) {
                    genericItem.enchantements = new HashMap<String, Integer>();
                    for (String enchmnt : generic_item.getConfigurationSection("enchantements").getKeys(false))
                        try {
                            genericItem.enchantements.put(enchmnt, generic_item.getConfigurationSection("enchantements").getInt(enchmnt));
                        } catch (NullPointerException e) {
                            System.out.println(ChatColor.YELLOW + "Зачарование введено неверным образом. Не удалось его добавить к предмету. " + enchmnt);
                        }
                }
                //ATTRIBUTES
                genericItem.attributes = new ArrayList<mAttribute>();
                if (generic_item.contains("attributes")) {
                    for (String attr_key : generic_item.getConfigurationSection("attributes").getKeys(false)) {
                        if (generic_item.contains("attributes." + attr_key + ".name"))
                            genericItem.attributes.add(
                                    new mAttribute(
                                            generic_item.getString("attributes." + attr_key + ".name"),
                                            generic_item.getDouble("attributes." + attr_key + ".amount", 0.0),
                                            generic_item.getString("attributes." + attr_key + ".equipment_slot", "HAND")
                                    )
                            );
                    }
                }
                //INTERACT EVENTS
                if (generic_item.contains("interact")) {
                    genericItem.events = new ArrayList<InteractEvent>();
                    for (String event_key : generic_item.getConfigurationSection("interact").getKeys(false)) {
                        if (event_key.equals("RIGHT_CLICK")
                                || event_key.equals("LEFT_CLICK")
                                || event_key.equalsIgnoreCase("drink")
                                || event_key.equalsIgnoreCase("eat")
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

                            for (String effects : event.getStringList("remove_potion_effect"))
                                if (PotionEffectType.getByName(effects) != null)
                                    interactEvent.remove_potion_effect.add(PotionEffectType.getByName(effects));
                                else
                                    System.out.println(ChatColor.YELLOW + "Неподдерживаемый удаляемый эффект: " + effects);

                            if (event.contains("potion_effect")) {
                                interactEvent.potion_effects = new ArrayList<PotionEffect>();
                                for (String potion_key : event.getConfigurationSection("potion_effect").getKeys(false)) {
                                    if (PotionEffectType.getByName(potion_key) == null) {
                                        System.out.println(ChatColor.YELLOW + "Введен неверный эффект" + potion_key);
                                        continue;
                                    }
                                    interactEvent.potion_effects.add(new PotionEffect(
                                            PotionEffectType.getByName(potion_key),
                                            event.getConfigurationSection("potion_effect").getConfigurationSection(potion_key).getInt("duration", 1),
                                            event.getConfigurationSection("potion_effect").getConfigurationSection(potion_key).getInt("amplifier", 1)
                                    ));
                                }
                            }
                            genericItem.events.add(interactEvent);
                        } else {
                            System.out.println(ChatColor.YELLOW + "Введен неподдерживаемый эвент " + event_key);
                        }
                    }
                }
                CreateItem(genericItem, key);
                if (DEBUG_ITEM)
                    genericItem.PrintItem();
            }
        }
    }


    private boolean reload(CommandSender sender) {
        if (!sender.hasPermission("empireitems.reload")) {
            sender.sendMessage(ChatColor.RED + "У вас нет разрешения использовать эту команду");
            return true;
        }
        sender.sendMessage(ChatColor.GREEN + "Перезагружаем EmpireItems");

        Bukkit.clearRecipes();
        generic_item.reloadConfig();
        EnableFunc();
        genericListener.ReloadListener(item_events);
        sender.sendMessage(ChatColor.GREEN + "Плагин успешно перезагружен!");
        return true;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (label.equalsIgnoreCase("ereload"))
            return reload(sender);
        if (label.equalsIgnoreCase("ezip")) {
            itemManager.print();
            itemManager.GenerateMinecraftModels();
            System.out.println(getDataFolder() + "\\pack\\assets");
        }
        if (label.equalsIgnoreCase("emgui")) {
            new EmpireCategoriesMenu(getPlayerMenuUtility((Player) sender), this).open();
        }
        if (label.equalsIgnoreCase("empireitems") || label.equalsIgnoreCase("emp")) {
            if (args.length == 0) {
                //todo
            }
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("reload"))
                    return reload(sender);

                if (args[0].equalsIgnoreCase("give") && args[1] != null) {
                    Player player = (Player) sender;
                    if (items.containsKey(args[1]))
                        player.getInventory().addItem(items.get(args[1]));
                    else {
                        sender.sendMessage(ChatColor.YELLOW + "Такого предмета нет:" + args[1]);
                        System.out.println(ChatColor.YELLOW + "Такого предмета нет:" + args[1]);
                    }
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


    private ItemStack CreateItem(GenericItem genericItem, String key) {
        ItemStack item = new ItemStack(Material.getMaterial(genericItem.material));


        ItemMeta meta = item.getItemMeta();

        for (String flag:genericItem.itemFlags)
            if (ItemFlag.valueOf(flag)!=null)
                meta.addItemFlags(ItemFlag.valueOf(flag));
        for (mAttribute attr : genericItem.attributes) {
            try {
                meta.addAttributeModifier(
                        Attribute.valueOf(attr.name),
                        new AttributeModifier(
                                UUID.randomUUID(),
                                Attribute.valueOf(attr.name).name(),
                                attr.amount,
                                AttributeModifier.Operation.ADD_NUMBER,
                                EquipmentSlot.valueOf(attr.equipment_slot)

                        )
                );
            } catch (Exception e) {
                System.out.println(ChatColor.YELLOW + "Не удалось создать атрибут " + attr.name);
            }
        }
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

        NamespacedKey durabilityMechanicNamespace = new NamespacedKey(this,"durability");
        meta.getPersistentDataContainer().set(durabilityMechanicNamespace, PersistentDataType.INTEGER,10);

        item.setItemMeta(meta);
        if (genericItem.material.equalsIgnoreCase("potion")) {
            NBTItem nbtItem = new NBTItem(item);
            nbtItem.setInteger("CustomPotionColor", 167167167);
            nbtItem.applyNBT(item);
            item = nbtItem.getItem();
        }

//        if (genericItem.durability!=-1) {
//            System.out.println(genericItem.display_name+" ="+genericItem.durability);
//            NBTItem nbtItem = new NBTItem(item);
//            nbtItem.setInteger("Damage", genericItem.durability);
//            nbtItem.applyNBT(item);
//            item = nbtItem.getItem();
//        }
        items.put(key, item);
        if (genericItem.pattern != null) {

            ShapedRecipe recipe = getGenericRecipe(genericItem, item);
            if (recipe != null)
                try {
                    Bukkit.addRecipe(recipe);
                } catch (IllegalStateException e) {
                    System.out.println(ChatColor.RED + "Проверьте рецепт и permission предмета: " + genericItem.display_name);
                }

        }
        return item;
    }

    private ShapedRecipe getGenericRecipe(GenericItem genericItem, ItemStack item) {
        NamespacedKey key = new NamespacedKey(this, genericItem.material + genericItem.permission);
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        if (genericItem.pattern != null) {
            recipe.shape(genericItem.pattern.get(0).toString(), genericItem.pattern.get(1).toString(), genericItem.pattern.get(2).toString());
            for (char keys : genericItem.ingredients.keySet())
                if (Material.getMaterial(genericItem.ingredients.get(keys)) != null) {
                    recipe.setIngredient(keys, Material.getMaterial(genericItem.ingredients.get(keys)));
                } else if (items.get(genericItem.ingredients.get(keys)) != null) {
                    recipe.setIngredient(keys, new RecipeChoice.ExactChoice(items.get(genericItem.ingredients.get(keys))));
                } else {
                    System.out.println(ChatColor.YELLOW + "Введен неверный материал");
                    recipe = null;
                    break;
                }

        }
        itemRecipe.put(item, recipe);
        return recipe;
    }


}
