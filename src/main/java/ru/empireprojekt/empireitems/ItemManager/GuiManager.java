package ru.empireprojekt.empireitems.ItemManager;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.empireprojekt.empireitems.EmpireItems;

import java.util.ArrayList;
import java.util.List;

public class GuiManager implements CommandExecutor {
    private EmpireItems plugin;
    private List<GuiItems> guiItems;
    private String GuiTextMain="";
    public class GuiItems {
        String name = "";
        String icon = "";
        String permission = "";
        public List<String> items = new ArrayList<String>();

        GuiItems(String name, String icon, String permission, List<String> items) {
            this.name = name;
            this.icon = icon;
            this.permission = permission;
            this.items = items;
        }
        List<String> getItems(){
            return items;
        }

    }

    public GuiManager(EmpireItems plugin) {
        this.plugin = plugin;
        guiItems = new ArrayList<GuiItems>();
        FileConfiguration guis = plugin.generic_item.getGuiConfig();
        GuiTextMain = guis.getString("categories.text","Null");
        for (String key : guis.getConfigurationSection("categories").getKeys(false)) {
            ConfigurationSection sect = guis.getConfigurationSection("categories").getConfigurationSection(key);
            System.out.println(key);
            guiItems.add(new GuiItems(
                    sect.getString("name"),
                    sect.getString("icon"),
                    sect.getString("permission"),
                    sect.getStringList("items")
            ));
        }
    }


    private ItemStack[] getGuiItems(){//Переделать
        ItemStack [] itemStacks = new ItemStack[ guiItems.get(0).items.size()];
        int i=0;
        for (String item: guiItems.get(0).items) {
            itemStacks[i++] = plugin.items.get(item);
            System.out.println("Add item"+item);
        }

        System.out.println("ItemsLen"+itemStacks.length);
        return itemStacks;

    }
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            int size = guiItems.size();

            Inventory gui = Bukkit.createInventory(player,9,GuiTextMain);

            gui.setContents(getGuiItems());
            player.openInventory(gui);
        }
        return true;
    }
}
