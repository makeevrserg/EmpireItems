package ru.empireprojekt.empireitems.events;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import ru.empireprojekt.empireitems.EmpireConstants;
import ru.empireprojekt.empireitems.EmpireItems;
import ru.empireprojekt.empireitems.ItemManager.menusystem.menu.EmpireCategoriesMenu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CommandManager implements CommandExecutor {

    EmpireItems plugin;
    EmpireItems.PluginSettings settings;
    EmpireConstants CONSTANTS;

    public CommandManager(EmpireItems plugin) {
        this.plugin = plugin;
        settings = plugin.mSettings;
        CONSTANTS = plugin.CONSTANTS;
        plugin.getCommand("ereload").setExecutor(this);
        plugin.getCommand("ezip").setExecutor(this);
        plugin.getCommand("emgui").setExecutor(this);
        plugin.getCommand("emreplace").setExecutor(this);
        plugin.getCommand("emrepair").setExecutor(this);
        plugin.getCommand("emsplash").setExecutor(this);
        plugin.getCommand("emnbt").setExecutor(this);
        plugin.getCommand("emojis").setExecutor(this);
        plugin.getCommand("emp").setExecutor(this);
        plugin.getCommand("empireitems").setExecutor(this);
        plugin.getCommand("ezip").setExecutor(this);

    }


    public void CheckReplaceItem(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        ItemMeta meta = itemInHand.getItemMeta();
        if (meta != null && meta.getPersistentDataContainer().has(CONSTANTS.empireID, PersistentDataType.STRING)) {
            String id = meta.getPersistentDataContainer().get(CONSTANTS.empireID, PersistentDataType.STRING);
            if (!plugin.getEmprieItems().containsKey(id)) {
                player.sendMessage(CONSTANTS.HEXPattern("&cПредмет в руке не подходит для замены!"));
                return;
            }

            ItemStack itemToCraft = plugin.getEmprieItems().get(id);
            int amount = itemInHand.getAmount();
            itemInHand = itemToCraft.clone();
            itemInHand.setAmount(amount);
            player.getInventory().setItemInMainHand(itemInHand);
            player.sendMessage(CONSTANTS.HEXPattern("&2Предмет успешно заменен"));

        } else {
            player.sendMessage(CONSTANTS.HEXPattern("&cПредмет в руке не подходит для замены!"));
        }

    }

    public void PlaySplash(Player p, String splash) {
        ItemStack totemItem = plugin.items.get(splash);
        if (p == null || totemItem == null) {
            System.out.println(ChatColor.AQUA + "[EmpireItems]" + "Splash отсутствует либо игрок=null!: " + splash);
            return;
        }
        ItemStack itemInHand = p.getInventory().getItemInMainHand().clone();
        p.getInventory().setItemInMainHand(totemItem);
        p.playEffect(EntityEffect.TOTEM_RESURRECT);
        p.getInventory().setItemInMainHand(itemInHand);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (label.equalsIgnoreCase("ereload"))
            return plugin.reload(sender);
        if (label.equalsIgnoreCase("ezip")) {
            //itemManager.print();
            plugin.getItemManager().GenerateMinecraftModels();
            System.out.println(ChatColor.AQUA + "[EmpireItems]" + plugin.getDataFolder() + File.separator + "pack" + File.separator + "assets");
        }
        if (label.equalsIgnoreCase("emgui")) {
            new EmpireCategoriesMenu(plugin.getPlayerMenuUtility((Player) sender), plugin).open();
        }
        if ((label.equalsIgnoreCase("emreplace") || label.equalsIgnoreCase("emr")) && (sender instanceof Player)) {
            CheckReplaceItem((Player) sender);
        }
        if (label.equalsIgnoreCase("emrepair")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                ItemStack item = player.getInventory().getItemInMainHand();
                ItemMeta meta = item.getItemMeta();
                if (meta != null && plugin.getServer().getPluginManager().getPlugin("NBTAPI") != null) {
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
            if (Bukkit.getPlayer(args[0]) != null && plugin.getEmprieItems().containsKey(args[1])) {
                PlaySplash(Bukkit.getPlayer(args[0]), args[1]);
            } else {
                sender.sendMessage(ChatColor.RED + "Неверные значения");
            }
        }

        //emnbt delete NbtName
        //emnbt set NbtName NbtValue
        if (sender.hasPermission("empireitems.changenbt") &&
                sender instanceof Player &&
                label.equalsIgnoreCase("emnbt") &&
                plugin.getServer().getPluginManager().getPlugin("NBTAPI") != null) {
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
            for (String emoji : plugin.getEmojis().keySet()) {
                count++;
                list += emoji + "  =  " + "&f" + plugin.getEmojis().get(emoji) + "&r" + "\n";
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
                    return plugin.reload(sender);

                if (args.length >= 3 && args[0].equalsIgnoreCase("give") && args[1] != null && args[2] != null) {
                    Player player = Bukkit.getPlayer(args[1]);
                    int count = 1;
                    if (args.length >= 4 && args[3] != null)
                        try {
                            count = Integer.parseInt(args[3]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(ChatColor.YELLOW + "Неверное значение");
                        }
                    if (player != null && plugin.getEmprieItems().containsKey(args[2]))
                        for (int i = 0; i < count; ++i)
                            player.getInventory().addItem(plugin.getEmprieItems().get(args[2]));
                    else {
                        sender.sendMessage(ChatColor.YELLOW + "Такого предмета или игрока нет:" + args[1] + ";" + args[2]);
                    }
                    //todo
                }
            }
        }
        return false;
    }
}
