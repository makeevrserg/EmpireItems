package ru.empireprojekt.empireitems;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.empireprojekt.empireitems.ItemManager.GenericItem;
import ru.empireprojekt.empireitems.ItemManager.ItemManager;
import ru.empireprojekt.empireitems.ItemManager.mAttribute;
import ru.empireprojekt.empireitems.ItemManager.menusystem.MenuItems;
import ru.empireprojekt.empireitems.ItemManager.menusystem.MenuListener;
import ru.empireprojekt.empireitems.ItemManager.menusystem.PlayerMenuUtility;
import ru.empireprojekt.empireitems.events.CommandManager;
import ru.empireprojekt.empireitems.events.Drop;
import ru.empireprojekt.empireitems.events.GenericListener;
import ru.empireprojekt.empireitems.events.InteractEvent;
import ru.empireprojekt.empireitems.files.DataManager;
import ru.empireprojekt.empireitems.files.GenericItemManager;

import java.util.*;

public class EmpireItems extends JavaPlugin {

    public GenericItemManager generic_item;
    public Map<String, ItemStack> items;
    public HashMap<ItemStack, ShapedRecipe> itemRecipe;
    private Map<String, InteractEvent[]> item_events;
    private TabCompletition tabCompletition;
    public GenericListener genericListener;
    private ItemManager itemManager;
    public List<MenuItems> menuItems;
    public HashMap<String, Drop[]> mobDrops;
    public HashMap<String, Drop[]> blockDrops;
    private HashMap<Integer, String> usedModelData;
    private static final HashMap<Player, PlayerMenuUtility> playerMenuUtilityMap = new HashMap<>();
    private MenuListener menuListener;
    private int maxCustomModelDate = 0;
    public EmpireConstants CONSTANTS;

    public PluginSettings mSettings;
    public HashMap<String, List<itemUpgradeClass>> itemUpgradesInfo;
    public HashMap<String, Double> itemUpgradeCostDecreaser;
    HashMap<String, String> emojis;
    CommandManager empireCommandManager;
    DataManager guiConfig;
    DataManager dropsConfig;

    public HashMap<String, String> getEmojis() {
        return emojis;
    }

    public Map<String, ItemStack> getEmprieItems() {
        return items;
    }

    public GenericListener getGenericListener() {
        return genericListener;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public static class itemUpgradeClass {
        public String attribute;
        public String slot;
        public double max_add;
        public double min_add;
    }

    public static class PluginSettings {
        public boolean isUpgradeEnabled = true;
        public double upgradeCostMultiplier = 1.0;
        public double vampirismMultiplier = 0.05;
    }


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


    public DataManager getGuiConfig() {
        return guiConfig;
    }
    public DataManager getDropsConfig(){
        return dropsConfig;
    }
    private void generateMenuItems() {
        menuItems = new ArrayList<>();
        ConfigurationSection categories = guiConfig.getConfig().getConfigurationSection("categories");
        if (categories != null)
            for (String key : categories.getKeys(false)) {
                ConfigurationSection sect = categories.getConfigurationSection(key);
                assert sect != null;
                menuItems.add(
                        new MenuItems(sect.getString("title", "null"),
                                sect.getString("name", "null"),
                                sect.getString("icon", "STONE"),
                                sect.getStringList("lore"),
                                sect.getString("permission", "null"),
                                sect.getStringList("items")
                        ));
            }
    }

    @Override
    public void onEnable() {
        System.out.println(ChatColor.GREEN + "-----------------------------------------------------------");
        System.out.println(ChatColor.AQUA + "[EmpireItems]" + "Enabling EmpireItems v" + getDescription().getVersion());
        EnableFunc();
        System.out.println(ChatColor.AQUA + "[EmpireItems]" + "Plugin EmpireItems has been Enabled!");
        System.out.println(ChatColor.GREEN + "-----------------------------------------------------------");

    }


    private void GenerateEmoji() {
        emojis = new HashMap<>();
        if (guiConfig.getConfig().contains("emoji"))
            for (String key : guiConfig.getConfig().getConfigurationSection("emoji").getKeys(false)) {
                emojis.put(
                        ":" + key + ":",
                        guiConfig.getConfig().getString("emoji." + key)
                );
            }
    }

    //Функция для перезапуска, не надо трогать
    private void EnableFunc() {
        CONSTANTS = new EmpireConstants(this);
        if (getServer().getPluginManager().getPlugin("NBTAPI") != null)
            System.out.println(CONSTANTS.PLUGIN_MESSAGE + ChatColor.GREEN + "Item-NBT-API Найден");
        else
            System.out.println(CONSTANTS.PLUGIN_MESSAGE + ChatColor.RED + "Item-NBT-API не Найден");


        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null)
            System.out.println(CONSTANTS.PLUGIN_MESSAGE + ChatColor.GREEN + "PlaceholderAPI Найден");
        else
            System.out.println(CONSTANTS.PLUGIN_MESSAGE + ChatColor.RED + "PlaceholderAPI не Найден");

        if (getServer().getPluginManager().getPlugin("DiscordSRV") != null)
            System.out.println(CONSTANTS.PLUGIN_MESSAGE + ChatColor.GREEN + "DiscordSRV Найден");
        else
            System.out.println(CONSTANTS.PLUGIN_MESSAGE + ChatColor.RED + "DiscordSRV не Найден");

        if (getServer().getPluginManager().getPlugin("ProtocolLib") != null)
            System.out.println(CONSTANTS.PLUGIN_MESSAGE + ChatColor.GREEN + "ProtocolLib Найден");
        else
            System.out.println(CONSTANTS.PLUGIN_MESSAGE + ChatColor.RED + "ProtocolLib не Найден");

        itemUpgradesInfo = new HashMap<>();
        itemUpgradeCostDecreaser = new HashMap<>();
        itemManager = new ItemManager(this);
        item_events = new HashMap<>();
        usedModelData = new HashMap<>();
        items = new HashMap<>();
        itemRecipe = new HashMap<>();
        mobDrops = new HashMap<>();
        blockDrops = new HashMap<>();

        mSettings = new PluginSettings();

        this.generic_item = new GenericItemManager(this);
        guiConfig = new DataManager("gui.yml", this);
        mSettings.isUpgradeEnabled = guiConfig.getConfig().getBoolean("settings.isUpgradeEnabled", true);
        mSettings.upgradeCostMultiplier = guiConfig.getConfig().getDouble("settings.upgradeCostMultiplier", 1.0);
        mSettings.vampirismMultiplier = guiConfig.getConfig().getDouble("settings.vampirismMultiplier", 0.05);

        dropsConfig = new DataManager("z_drops.yml",this);

        LoadGenericItems();
        if (genericListener != null)
            genericListener.UnregisterListener();
        genericListener = new GenericListener(this, item_events);
        getServer().getPluginManager().registerEvents(genericListener, this);
        tabCompletition = new TabCompletition(itemManager.GetNames());
        getCommand("emp").setTabCompleter(tabCompletition);
        getCommand("empireitems").setTabCompleter(tabCompletition);
        GenerateEmoji();

        System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.GREEN + "Item upgrade enabled:" + mSettings.isUpgradeEnabled);
        System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.GREEN + "Upgrade Cost multiplier:" + mSettings.upgradeCostMultiplier);
        generateMenuItems();
        if (menuListener != null)
            InventoryClickEvent.getHandlerList().unregister(menuListener);
        menuListener = new MenuListener();
        getServer().getPluginManager().registerEvents(menuListener, this);
        empireCommandManager = new CommandManager(this);
    }


    //Создание дропа предметов drops.yml <String=Name of Entity,Drop=drop>
    private void getDrop(HashMap<String, Drop[]> map, ConfigurationSection section) {
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

    //Загрузка предметов из файлов
    private void LoadGenericItems() {


        //Лут с мобов находится под полем loot
        if (getDropsConfig().getConfig().contains("loot")) {
            System.out.println("Drop");
            ConfigurationSection loot = getDropsConfig().getConfig().getConfigurationSection("loot");
            if (loot != null && loot.contains("mobs")) {
                ConfigurationSection mobs = loot.getConfigurationSection("mobs");
                getDrop(mobDrops, mobs);
            }
            if (loot != null && loot.contains("blocks")) {
                ConfigurationSection blocks = loot.getConfigurationSection("blocks");
                getDrop(blockDrops, blocks);
            }
        }

        //Получаем списко всех .yml файлов
        List<FileConfiguration> file_generic_items = generic_item.getConfig();
        for (FileConfiguration file_generic_item : file_generic_items) {
            //если это файл с предметами, он должен быть под поле yml_items
            ConfigurationSection yml_items = file_generic_item.getConfigurationSection("yml_items");
            if (yml_items == null) {
                System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.RED + "В файле" + file_generic_item.getName() + " отсутствует поле yml_items.");
                continue;
            }
            //Получаем все поля предметов из yml файла
            for (String key : yml_items.getKeys(false)) {
                //System.out.println("Предмет: " + key);
                ConfigurationSection generic_item = yml_items.getConfigurationSection(key);
                GenericItem genericItem = new GenericItem();
                //Проверяем на наличие необходимых значений
                if (!generic_item.contains("display_name")
                        || !generic_item.contains("permission")
                        || !generic_item.contains("enabled")
                        || !generic_item.contains("material")
                        || !generic_item.contains("custom_model_data")
                        || (!generic_item.contains("texture_path") && !generic_item.contains("model_path"))
                ) {
                    System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.RED + "Предмет " + key + " не имеет одного из необходимых значений:");
                    System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.RED + "display_name permission enabled  material custom_model_data (texture_path or model_path)");
                    continue;
                }
                if (generic_item.contains("UPGRADE_COST_DECREASE")) {
                    itemUpgradeCostDecreaser.put(key, generic_item.getDouble("UPGRADE_COST_DECREASE"));
                }
                if (generic_item.contains("upgrade")) {
                    List<itemUpgradeClass> mUpgrades = new ArrayList<>();
                    for (String itemAttribyte : generic_item.getConfigurationSection("upgrade").getKeys(false)) {
                        itemUpgradeClass upgrade = new itemUpgradeClass();
                        upgrade.attribute = itemAttribyte;
                        upgrade.max_add = generic_item.getConfigurationSection("upgrade").getConfigurationSection(itemAttribyte).getDouble("max_add", 0.0);
                        upgrade.min_add = generic_item.getConfigurationSection("upgrade").getConfigurationSection(itemAttribyte).getDouble("min_add", 0.0);
                        upgrade.slot = generic_item.getConfigurationSection("upgrade").getConfigurationSection(itemAttribyte).getString("slot", "HAND");
                        mUpgrades.add(upgrade);
                    }
                    itemUpgradesInfo.put(key, mUpgrades);

                }
                genericItem.itemId = key;
                genericItem.isBlock = generic_item.getBoolean("isBlock", false);
                genericItem.display_name = CONSTANTS.HEXPattern(generic_item.getString("display_name"));
                genericItem.lore = generic_item.getStringList("lore");

                CONSTANTS.HEXPattern(genericItem.lore);
                genericItem.permission = generic_item.getString("permission");
                genericItem.material = generic_item.getString("material");
                genericItem.customModelData = generic_item.getInt("custom_model_data");
                if (genericItem.customModelData > maxCustomModelDate)
                    maxCustomModelDate = genericItem.customModelData;
                genericItem.enabled = generic_item.getBoolean("enabled");
                genericItem.itemFlags = new ArrayList<String>();
                genericItem.itemFlags = generic_item.getStringList("item_flags");
                genericItem.durability = generic_item.getInt("durability", -1);
                if (usedModelData.containsKey(genericItem.customModelData))
                    System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.YELLOW + " AlreadyUsed CustomModelData " + usedModelData.get(genericItem.customModelData) + "=" + genericItem.itemId);
                else
                    usedModelData.put(genericItem.customModelData, genericItem.itemId);
                if (generic_item.contains("texture_path") && generic_item.contains("model_path"))
                    System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.YELLOW + "Вы используете model_path и texture_path в " + key + ". Используйте что-то одно.");
                genericItem.texture_path = generic_item.getString("texture_path");
                genericItem.model_path = generic_item.getString("model_path");

                //Чекаем, если предмет содержит крафт
                if (generic_item.contains("ingredients") && generic_item.contains("pattern")) {
                    genericItem.ingredients = new HashMap<Character, String>();
                    genericItem.pattern = generic_item.getList("pattern");
                    for (String k : generic_item.getConfigurationSection("ingredients").getKeys(false)) {
                        genericItem.ingredients.put(k.charAt(0), generic_item.getConfigurationSection("ingredients").getString(k));
                    }

                }

                //Добавляем предмет в json файла ресурс-пака
                itemManager.AddItem(file_generic_item.getString("namespace", "empire_items"), genericItem.texture_path, genericItem.model_path, key, genericItem.material, genericItem.customModelData, genericItem.isBlock);

                //Зачарования
                if (generic_item.contains("enchantements")) {
                    genericItem.enchantements = new HashMap<String, Integer>();
                    for (String ench : generic_item.getConfigurationSection("enchantements").getKeys(false))
                        genericItem.enchantements.put(ench, generic_item.getConfigurationSection("enchantements").getInt(ench, 1));

                }
                //ATTRIBUTES
                genericItem.attributes = new ArrayList<mAttribute>();
                if (generic_item.contains("attributes"))
                    for (String attr_key : generic_item.getConfigurationSection("attributes").getKeys(false))
                        if (generic_item.contains("attributes." + attr_key + ".name"))
                            genericItem.attributes.add(
                                    new mAttribute(
                                            generic_item.getString("attributes." + attr_key + ".name"),
                                            generic_item.getDouble("attributes." + attr_key + ".amount", 0.0),
                                            generic_item.getString("attributes." + attr_key + ".equipment_slot", "HAND")
                                    )
                            );


                //INTERACT EVENTS
                if (generic_item.contains("interact")) {
                    List<InteractEvent> interactEvents = new ArrayList<>();
                    for (String event_key : generic_item.getConfigurationSection("interact").getKeys(false)) {
                        //Чекаем на поддерживаемый эвент

                        if (
                                event_key.equalsIgnoreCase("LEFT_CLICK_AIR") ||
                                        event_key.equalsIgnoreCase("RIGHT_CLICK_AIR") ||
                                        event_key.equalsIgnoreCase("RIGHT_CLICK_BLOCK") ||
                                        event_key.equalsIgnoreCase("LEFT_CLICK_BLOCK") ||
                                        event_key.equalsIgnoreCase("PHYSICAL") ||
                                        event_key.equalsIgnoreCase("item_hit_ground")
                                        || event_key.equalsIgnoreCase("drink")
                                        || event_key.equalsIgnoreCase("eat")
                                        || event_key.equalsIgnoreCase("entity_damage")
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
                            interactEvent.execute_commands = event.getStringList("execute_commands").toArray(interactEvent.execute_commands);
                            interactEvent.takeDurability = event.getInt("durability", 0);
                            if (event.contains("explosion.power"))
                                interactEvent.explosionPower = event.getInt("explosion.power");


                            //Проверяем на наличие удаляемых эффектов
                            List<PotionEffectType> removePotionEffectsTypes = new ArrayList<>();
                            for (String effects : event.getStringList("remove_potion_effect"))
                                if (PotionEffectType.getByName(effects) != null)
                                    removePotionEffectsTypes.add(PotionEffectType.getByName(effects));
                                else
                                    System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.YELLOW + "Неподдерживаемый удаляемый эффект: " + effects);
                            if (removePotionEffectsTypes.size() > 0)
                                interactEvent.remove_potion_effect = removePotionEffectsTypes.toArray(interactEvent.remove_potion_effect);
                            //Проверяем на наличие эффектов
                            if (event.contains("potion_effect")) {
                                interactEvent.potion_effects = new ArrayList<PotionEffect>();
                                for (String potion_key : event.getConfigurationSection("potion_effect").getKeys(false)) {
                                    if (PotionEffectType.getByName(potion_key) == null) {
                                        System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.YELLOW + "Введен неверный эффект" + potion_key);
                                        continue;
                                    }
                                    interactEvent.potion_effects.add(new PotionEffect(
                                            PotionEffectType.getByName(potion_key),
                                            event.getConfigurationSection("potion_effect").getConfigurationSection(potion_key).getInt("duration", 1),
                                            event.getConfigurationSection("potion_effect").getConfigurationSection(potion_key).getInt("amplifier", 1)
                                    ));
                                }
                            }
                            interactEvents.add(interactEvent);
                        } else {
                            System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.YELLOW + "Введен неподдерживаемый эвент " + event_key + " item=" + genericItem.itemId);
                        }
                    }
                    genericItem.events = new InteractEvent[0];
                    genericItem.events = interactEvents.toArray(genericItem.events);
                }

                //EmpireEnchants
                genericItem.empireEnchants = new HashMap<>();
                if (generic_item.contains("empire_enchants.hammer"))
                    genericItem.empireEnchants.put(CONSTANTS.itemHammer,generic_item.getInt("empire_enchants.hammer"));
                if (generic_item.contains("empire_enchants.lavaWalker"))
                    genericItem.empireEnchants.put(CONSTANTS.lavaWalker,generic_item.getInt("empire_enchants.lavaWalker"));
                if (generic_item.contains("empire_enchants.vampirism"))
                    genericItem.empireEnchants.put(CONSTANTS.vampirism,generic_item.getInt("empire_enchants.vampirism"));


                items.put(key, CreateItem(genericItem, key));

                if (generic_item.getBoolean("debug", false))
                    genericItem.PrintItem();
            }
        }
        System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.GREEN + "Последняя custom_model_data = " + maxCustomModelDate);
    }


    private void DisablePlugin() {

        if (genericListener != null)
            genericListener.UnregisterListener();
        CONSTANTS.onDestroy();

        Iterator<Recipe> ite = getServer().recipeIterator();
        Recipe recipe;
        while (ite.hasNext()) {
            recipe = ite.next();
            if (recipe != null && itemRecipe.containsKey(recipe.getResult()))
                ite.remove();
        }
    }

    public boolean reload(CommandSender sender) {
        if (!sender.hasPermission("empireitems.reload")) {
            sender.sendMessage(ChatColor.RED + "У вас нет разрешения использовать эту команду");
            return true;
        }
        sender.sendMessage(ChatColor.GREEN + "Перезагружаем EmpireItems");
        DisablePlugin();
        generic_item.reloadConfig();
        EnableFunc();
        sender.sendMessage(ChatColor.GREEN + "Плагин успешно перезагружен!");
        return true;
    }


    @Override
    public void onDisable() {
        DisablePlugin();
        System.out.println(ChatColor.AQUA + "[EmpireItems]" + " Выкулючаем плагин");
        super.onDisable();
    }


    //Создание предмета из распарсенных значнеий
    private ItemStack CreateItem(GenericItem genericItem, String key) {
        ItemStack item = new ItemStack(Material.getMaterial(genericItem.material));
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        for (String flag : genericItem.itemFlags) {
            meta.addItemFlags(ItemFlag.valueOf(flag));
        }
        int i = -1;
        for (mAttribute attr : genericItem.attributes) {
            i += 1;
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
                System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.YELLOW + "Не удалось создать атрибут " + attr.name + " item=" + item + ";" + e.toString());
            }
        }
        meta.setDisplayName(genericItem.display_name);
        meta.setLore(genericItem.lore);
        meta.setCustomModelData(genericItem.customModelData);
        if (genericItem.enchantements != null) {
            for (String keys : genericItem.enchantements.keySet()) {
                if (Enchantment.getByKey(NamespacedKey.minecraft(keys.toLowerCase())) == null)
                    System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.RED + " Введено неверное зачарование " + keys + ". Используйте зачарования из minecraft, а не spigot!");
                else
                    meta.addEnchant(Enchantment.getByKey(NamespacedKey.minecraft(keys.toLowerCase())), genericItem.enchantements.get(keys), true);
            }
        }

        if (genericItem.events != null) {
            for (InteractEvent event : genericItem.events) {
                if (event.explosionPower > 0) {
                    meta.getPersistentDataContainer().set(CONSTANTS.itemExplosionNamespace, PersistentDataType.INTEGER, event.explosionPower);
                }

            }
        }

        if (genericItem.durability > 0) {
            meta.getPersistentDataContainer().set(CONSTANTS.durabilityMechanicNamespace, PersistentDataType.INTEGER, genericItem.durability);

            meta.getPersistentDataContainer().set(CONSTANTS.maxCustomDurability, PersistentDataType.INTEGER, genericItem.durability);
        }
        meta.getPersistentDataContainer().set(CONSTANTS.empireID, PersistentDataType.STRING, genericItem.itemId);

        if (genericItem.empireEnchants!=null){
            if (genericItem.empireEnchants.containsKey(CONSTANTS.itemHammer)){
                meta.getPersistentDataContainer().set(CONSTANTS.itemHammer,PersistentDataType.INTEGER,genericItem.empireEnchants.get(CONSTANTS.itemHammer));
            }
            if (genericItem.empireEnchants.containsKey(CONSTANTS.lavaWalker)){
                meta.getPersistentDataContainer().set(CONSTANTS.lavaWalker,PersistentDataType.INTEGER,genericItem.empireEnchants.get(CONSTANTS.lavaWalker));
            }
            if (genericItem.empireEnchants.containsKey(CONSTANTS.vampirism)){
                meta.getPersistentDataContainer().set(CONSTANTS.vampirism,PersistentDataType.INTEGER,genericItem.empireEnchants.get(CONSTANTS.vampirism));
            }
        }

        item.setItemMeta(meta);
        if (genericItem.material.equalsIgnoreCase("potion") && getServer().getPluginManager().getPlugin("NBTAPI") != null) {
            NBTItem nbtItem = new NBTItem(item);
            nbtItem.setInteger("CustomPotionColor", 167167167);
            nbtItem.applyNBT(item);
            item = nbtItem.getItem();
        }


        if (genericItem.events != null)
            item_events.put(genericItem.itemId, genericItem.events);

        if (genericItem.pattern != null) {
            ShapedRecipe recipe = getGenericRecipe(genericItem, item);
            if (recipe != null)
                try {
                    Bukkit.addRecipe(recipe);
                } catch (IllegalStateException e) {
                    System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.RED + "Проверьте рецепт и permission предмета: " + genericItem.display_name);
                }

        }
        return item;
    }

    private ShapedRecipe getGenericRecipe(GenericItem genericItem, ItemStack item) {
        NamespacedKey key = new NamespacedKey(this, genericItem.material + genericItem.permission);
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        if (genericItem.pattern != null) {
            recipe.shape(genericItem.pattern.get(0).toString(), genericItem.pattern.get(1).toString(), genericItem.pattern.get(2).toString());
            for (char keys : genericItem.ingredients.keySet()) {
                String itemKey = genericItem.ingredients.get(keys);
                if (Material.getMaterial(itemKey) != null) {
                    recipe.setIngredient(keys, Material.getMaterial(itemKey));
                } else if (items.containsKey(itemKey)) {
                    recipe.setIngredient(keys, new RecipeChoice.ExactChoice(items.get(itemKey)));
                } else {
                    System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.YELLOW + "Введен неверный материал: " + genericItem.itemId + ";" + itemKey + ":" + keys);
                    recipe = null;
                    break;
                }
            }

        }
        itemRecipe.put(item, recipe);
        return recipe;
    }


}
