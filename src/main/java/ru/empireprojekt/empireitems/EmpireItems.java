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
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
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
import ru.empireprojekt.empireitems.ItemManager.menusystem.menu.EmpireCategoriesMenu;
import ru.empireprojekt.empireitems.events.Drop;
import ru.empireprojekt.empireitems.events.GenericListener;
import ru.empireprojekt.empireitems.events.InteractEvent;
import ru.empireprojekt.empireitems.files.GenericItemManager;

import java.io.File;
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

    public static class itemUpgradeClass {
        public String attribute;
        public String slot;
        public double max_add;
        public double min_add;
    }

    public static class PluginSettings {
        public boolean isUpgradeEnabled = true;
        public double upgradeCostMultiplier = 1.0;
    }


    private static PlayerMenuUtility getPlayerMenuUtility(Player p) {
        PlayerMenuUtility playerMenuUtility;
        if (playerMenuUtilityMap.containsKey(p))
            return playerMenuUtilityMap.get(p);
        else {
            playerMenuUtility = new PlayerMenuUtility(p);
            playerMenuUtilityMap.put(p, playerMenuUtility);
            return playerMenuUtility;
        }
    }


    private void generateMenuItems() {
        menuItems = new ArrayList<>();
        ConfigurationSection categories = generic_item.getGuiConfig().getConfigurationSection("categories");
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
        if (generic_item.getGuiConfig().contains("emoji"))
            for (String key : generic_item.getGuiConfig().getConfigurationSection("emoji").getKeys(false)) {
                emojis.put(
                        ":" + key + ":",
                        generic_item.getGuiConfig().getString("emoji." + key)
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
        this.generic_item = new GenericItemManager(this);
        mobDrops = new HashMap<>();
        blockDrops = new HashMap<>();
        LoadGenericItems();
        if (genericListener != null)
            genericListener.UnregisterListener();
        genericListener = new GenericListener(this, item_events);
        getServer().getPluginManager().registerEvents(genericListener, this);
        tabCompletition = new TabCompletition(itemManager.GetNames());
        getCommand("emp").setTabCompleter(tabCompletition);
        getCommand("empireitems").setTabCompleter(tabCompletition);
        GenerateEmoji();
        mSettings = new PluginSettings();
        mSettings.isUpgradeEnabled = generic_item.getGuiConfig().getBoolean("settings.isUpgradeEnabled", true);
        mSettings.upgradeCostMultiplier = generic_item.getGuiConfig().getDouble("settings.upgradeCostMultiplier", 1.0);
        System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.GREEN + "Item upgrade enabled:" + mSettings.isUpgradeEnabled);
        System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.GREEN + "Upgrade Cost multiplier:" + mSettings.upgradeCostMultiplier);
        generateMenuItems();
        if (menuListener != null)
            InventoryClickEvent.getHandlerList().unregister(menuListener);
        menuListener = new MenuListener();
        getServer().getPluginManager().registerEvents(menuListener, this);

    }


    //Создание дропа предметов drops.yml <String=Name of Entity,Drop=drop>
    private void getDrop(HashMap<String, Drop[]> map, ConfigurationSection section) {
        for (String key_ : section.getKeys(false)) {
            ConfigurationSection sect = section.getConfigurationSection(key_);
            List<Drop> mDrops = new ArrayList<Drop>();
            if (sect!=null && !sect.contains("entity")) {
                System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.YELLOW + key_ + " Не содержит entity!");
            }
            for (String itemKey : sect.getConfigurationSection("items").getKeys(false)) {
                ConfigurationSection mItem = sect.getConfigurationSection("items." + itemKey);
                if (mItem!=null && mItem.contains("item"))
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
        //Получаем списко всех .yml файлов
        List<FileConfiguration> file_generic_items = generic_item.getConfig();
        for (FileConfiguration file_generic_item : file_generic_items) {
            //Лут с мобов находится под полем loot
            if (file_generic_item.contains("loot")) {
                ConfigurationSection loot = file_generic_item.getConfigurationSection("loot");
                if (loot!=null && loot.contains("mobs")) {
                    ConfigurationSection mobs = loot.getConfigurationSection("mobs");
                    getDrop(mobDrops, mobs);
                }
                if (loot!=null && loot.contains("blocks")) {
                    ConfigurationSection blocks = loot.getConfigurationSection("blocks");
                    getDrop(blockDrops, blocks);
                }
            }
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

    private boolean reload(CommandSender sender) {
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


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (label.equalsIgnoreCase("ereload"))
            return reload(sender);
        if (label.equalsIgnoreCase("ezip")) {
            //itemManager.print();
            itemManager.GenerateMinecraftModels();
            System.out.println(ChatColor.AQUA + "[EmpireItems]" + getDataFolder() + File.separator + "pack" + File.separator + "assets");
        }
        if (label.equalsIgnoreCase("emgui")) {
            new EmpireCategoriesMenu(getPlayerMenuUtility((Player) sender), this).open();
        }
        if ((label.equalsIgnoreCase("emreplace") || label.equalsIgnoreCase("emr")) && (sender instanceof Player)) {
            CheckReplaceItem((Player) sender);
        }
        if (label.equalsIgnoreCase("emrepair")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                ItemStack item = player.getInventory().getItemInMainHand();
                ItemMeta meta = item.getItemMeta();
                if (meta != null && getServer().getPluginManager().getPlugin("NBTAPI") != null) {
                    NBTItem nbtItem = new NBTItem(item);
                    int Damage = nbtItem.getInteger("Damage");
                    if (Damage == 0) {
                        PersistentDataContainer container = meta.getPersistentDataContainer();
                        if (container.get(CONSTANTS.maxCustomDurability, PersistentDataType.INTEGER) != null)
                            container.set(CONSTANTS.durabilityMechanicNamespace, PersistentDataType.INTEGER, container.get(CONSTANTS.maxCustomDurability, PersistentDataType.INTEGER));
                        item.setItemMeta(meta);
                    }
                } else
                    sender.sendMessage(ChatColor.AQUA + "[EmpireItems]" + ChatColor.RED + "Вы не держите кастомный предмет либо не подключен Item-NNT-API");
            }
        }
        if (label.equalsIgnoreCase("emsplash") && args.length == 2) {
            if (Bukkit.getPlayer(args[0]) != null && items.containsKey(args[1])) {
                genericListener.PlaySplash(Bukkit.getPlayer(args[0]), args[1]);
            } else {
                sender.sendMessage(ChatColor.RED + "Неверные значения");
            }
        }

        //emnbt delete NbtName
        //emnbt set NbtName NbtValue
        if (sender.hasPermission("empireitems.changenbt") &&
                sender instanceof Player &&
                label.equalsIgnoreCase("emnbt") &&
                getServer().getPluginManager().getPlugin("NBTAPI") != null) {
            Player player = (Player) sender;
            ItemStack item = player.getInventory().getItemInMainHand();
            NBTItem nbtItem = new NBTItem(item);
            if (args[0].equalsIgnoreCase("delete") && args[1] != null) {
                nbtItem.removeKey(args[1]);
            } else if (args[0].equalsIgnoreCase("set") && args[1] != null && args[2] != null && args[3] != null) {
                if (args[1].equalsIgnoreCase("int"))
                    try {
                        nbtItem.setInteger(args[2], Integer.valueOf(args[3]));

                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.YELLOW + "Неверное значение");
                    }
                else if (args[1].equalsIgnoreCase("string"))
                    nbtItem.setString(args[2], args[3]);

            }
            nbtItem.applyNBT(item);
        }
        if (sender instanceof Player && label.equalsIgnoreCase("emojis")) {
            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta meta = (BookMeta) book.getItemMeta();
            meta.setTitle(CONSTANTS.HEXPattern("&fЭмодзи"));
            meta.setAuthor("RomaRoman");
            String list = "";
            List<String> pages = new ArrayList<String>();
            int count = 0;
            for (String emoji : emojis.keySet()) {
                count++;
                list += emoji + "  =  " + "&f" + emojis.get(emoji) + "&r" + "\n";
                if (count % 14 == 0) {
                    pages.add(list);
                    list = "";
                }
            }
            pages.add(list);
            meta.setPages(CONSTANTS.HEXPattern(pages));

            book.setItemMeta(meta);
            ((Player) sender).getInventory().addItem(book);


        }
        if (sender.hasPermission("empireitems.empgive") && (label.equalsIgnoreCase("empireitems") || label.equalsIgnoreCase("emp"))) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("reload"))
                    return reload(sender);

                if (args.length >= 3 && args[0].equalsIgnoreCase("give") && args[1] != null && args[2] != null) {
                    Player player = Bukkit.getPlayer(args[1]);
                    int count = 1;
                    if (args.length >= 4 && args[3] != null)
                        try {
                            count = Integer.parseInt(args[3]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(ChatColor.YELLOW + "Неверное значение");
                        }
                    if (player != null && items.containsKey(args[2]))
                        for (int i = 0; i < count; ++i)
                            player.getInventory().addItem(items.get(args[2]));
                    else {
                        sender.sendMessage(ChatColor.YELLOW + "Такого предмета или игрока нет:" + args[1] + ";" + args[2]);
                    }
                    //todo
                }
            }
        }
        return false;
    }


    public void CheckReplaceItem(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        ItemMeta meta = itemInHand.getItemMeta();
        if (meta != null && meta.getPersistentDataContainer().has(CONSTANTS.empireID, PersistentDataType.STRING)) {
            String id = meta.getPersistentDataContainer().get(CONSTANTS.empireID, PersistentDataType.STRING);
            if (!items.containsKey(id)) {
                player.sendMessage(CONSTANTS.HEXPattern("&cПредмет в руке не подходит для замены!"));
                return;
            }

            ItemStack itemToCraft = items.get(id);
            int amount = itemInHand.getAmount();
            itemInHand = itemToCraft.clone();
            itemInHand.setAmount(amount);
            player.getInventory().setItemInMainHand(itemInHand);
            player.sendMessage(CONSTANTS.HEXPattern("&2Предмет успешно заменен"));

        } else {
            player.sendMessage(CONSTANTS.HEXPattern("&cПредмет в руке не подходит для замены!"));
        }

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
        assert meta!=null;
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
