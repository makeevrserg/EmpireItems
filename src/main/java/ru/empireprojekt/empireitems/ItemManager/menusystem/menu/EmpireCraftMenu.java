package ru.empireprojekt.empireitems.ItemManager.menusystem.menu;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import ru.empireprojekt.empireitems.EmpireItems;
import ru.empireprojekt.empireitems.ItemManager.menusystem.Menu;
import ru.empireprojekt.empireitems.ItemManager.menusystem.PlayerMenuUtility;
import ru.empireprojekt.empireitems.events.Drop;

import java.util.*;

public class EmpireCraftMenu extends Menu {
    EmpireItems plugin;
    String item;
    int slot;
    int page;

    public EmpireCraftMenu(PlayerMenuUtility playerMenuUtility, int slot, int page, EmpireItems plugin, String item) {
        super(playerMenuUtility);
        this.slot = slot;
        this.plugin = plugin;
        this.item = item;
        this.page = page;
    }

    public String getMenuName() {
        return item;
    }

    public int getSlots() {
        return 54;
    }

    public void handleMenu(InventoryClickEvent e) {
        if (e.getCurrentItem() != null) {
            if (e.getSlot() == 49) {
                new EmpireCategoryMenu(playerMenuUtility, plugin, slot, page).open();
            } else if (e.getSlot() == 43) {
                if (playerMenuUtility.getPlayer() != null && playerMenuUtility.getPlayer().hasPermission("empireitems.get") && plugin.items.containsKey(item))
                    playerMenuUtility.getPlayer().getInventory().addItem(plugin.items.get(item));
            }
        }
    }

    public void setMenuItems() {

        ShapedRecipe recipe = plugin.itemRecipe.get(plugin.items.get(item));


        inventory.setItem(25, plugin.items.get(item));


        if (recipe != null) {
            int loc = 13;
            int count = 1;
            List<ItemStack> it = new ArrayList<ItemStack>();
            for (String s : recipe.getShape()) {
                it.add(recipe.getIngredientMap().get(s.charAt(0)));
                it.add(recipe.getIngredientMap().get(s.charAt(1)));
                it.add(recipe.getIngredientMap().get(s.charAt(2)));
            }


            inventory.setItem(11, it.get(0));
            inventory.setItem(12, it.get(1));
            inventory.setItem(13, it.get(2));

            inventory.setItem(20, it.get(3));
            inventory.setItem(21, it.get(4));
            inventory.setItem(22, it.get(5));

            inventory.setItem(29, it.get(6));
            inventory.setItem(30, it.get(7));
            inventory.setItem(31, it.get(8));
        }
        ItemStack close, drop;
        if (plugin.items.containsKey(
                plugin.generic_item.getGuiConfig().getString("settings.close_btn", "close")
        ))
            close = plugin.items.get(plugin.generic_item.getGuiConfig().getString("settings.close_btn", "close"));
        else close = new ItemStack(Material.BARRIER);

        inventory.setItem(49, close);


        if (plugin.items.containsKey(
                plugin.generic_item.getGuiConfig().getString("settings.close_btn", "close")
        ))
            drop = plugin.items.get(plugin.generic_item.getGuiConfig().getString("settings.drop_btn", "drop"));
        else drop = new ItemStack(Material.BARRIER);
        ItemMeta dropMeta = drop.getItemMeta();

        List<String> dropLore = new ArrayList<String>();

        if (IsDropped(plugin.mobDrops))
            dropLore = getDrops(dropLore, plugin.mobDrops);
        if (IsDropped(plugin.blockDrops))
            dropLore = getDrops(dropLore, plugin.blockDrops);

        if (dropLore.size() != 0) {

            dropMeta.setDisplayName(ChatColor.WHITE + "Выпадает из:");
            dropMeta.setLore(dropLore);
            drop.setItemMeta(dropMeta);

            inventory.setItem(46, drop);
        }
        if (playerMenuUtility.getPlayer() != null && playerMenuUtility.getPlayer().hasPermission("getItem")) {
            ItemStack getItem;
            if (plugin.items.containsKey(
                    plugin.generic_item.getGuiConfig().getString("settings.give_btn")
            ))
                getItem = plugin.items.get(plugin.generic_item.getGuiConfig().getString("settings.give_btn"));
            else
                getItem = new ItemStack(Material.STONE);
            inventory.setItem(43, getItem);
        }
    }

    private List<String> getDrops(List<String> dropLore, HashMap<String, List<Drop>> standartDrops) {
        HashMap<String, List<Drop>> mDrops = new HashMap<>();

        for (String key : standartDrops.keySet()) {
            List<Drop> drops = new ArrayList<>();
            for (Drop s : standartDrops.get(key))
                if (item.equals(s.item)) {
                    drops.add(s);
                }
            if (drops.size() > 0)
                mDrops.put(key, drops);
        }

        for (String key : mDrops.keySet()) {
            dropLore.add(ChatColor.DARK_GRAY + key + ":\n");
            for (Drop s : mDrops.get(key))
                dropLore.add(ChatColor.DARK_GRAY + "От " + s.min_amount + " до " + s.max_amount + " с вер." + " " + s.chance + "%\n");
        }
        return dropLore;
    }

    private boolean IsDropped(HashMap<String, List<Drop>> drop) {
        for (String key : drop.keySet())
            for (Drop s : drop.get(key))
                if (item.equals(s.item))
                    return true;


        return false;
    }
}
