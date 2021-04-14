package ru.empireprojekt.empireitems.ItemManager.menusystem.menu;

import com.iwebpp.crypto.TweetNaclFast;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import ru.empireprojekt.empireitems.EmpireItems;
import ru.empireprojekt.empireitems.ItemManager.menusystem.Menu;
import ru.empireprojekt.empireitems.ItemManager.menusystem.PlayerMenuUtility;
import ru.empireprojekt.empireitems.events.Drop;
import ru.empireprojekt.empireitems.events.GenericListener;
import ru.empireprojekt.empireitems.events.ItemUpgradeEvent;

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
        return plugin.CONSTANTS.HEXPattern(plugin.getGuiConfig().getConfig().getString("settings.workbench_ui", "Крафт") + plugin.items.get(item).getItemMeta().getDisplayName());
    }

    public int getSlots() {
        return 54;
    }

    public void handleMenu(InventoryClickEvent e) {

        if (e.getCurrentItem() != null) {
            System.out.println(e.getSlot());
            if (e.getSlot() == 49) {
                new EmpireCategoryMenu(playerMenuUtility, plugin, slot, page).open();
            } else if (e.getSlot() == 43) {
                System.out.println("1");
                if (playerMenuUtility.getPlayer() == null || !playerMenuUtility.getPlayer().hasPermission("empireitems.give"))
                    return;
                System.out.println("2 " + item+";"+plugin.items.containsKey(item));
                if (plugin.items.containsKey(item))
                    playerMenuUtility.getPlayer().getInventory().addItem(plugin.items.get(item));
                else {
                    Material material = Material.getMaterial(item);
                    if (material != null) {
                        ItemStack itemStack = new ItemStack(material, 1);
                        playerMenuUtility.getPlayer().getInventory().addItem(itemStack);
                    }
                }
            }
        }
    }


    private List<ItemStack> getItemRecipe() {
        ShapedRecipe recipe = plugin.itemRecipe.get(plugin.items.get(item));
        if (recipe != null) {
            List<ItemStack> it = new ArrayList<ItemStack>();
            for (String s : recipe.getShape()) {
                it.add(recipe.getIngredientMap().get(s.charAt(0)));
                it.add(recipe.getIngredientMap().get(s.charAt(1)));
                it.add(recipe.getIngredientMap().get(s.charAt(2)));
            }
            return it;
        }
        return null;
    }

    public void setMenuItems() {
        inventory.setItem(25, plugin.items.get(item));
        List<ItemStack> it = getItemRecipe();
        if (it != null) {


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
                plugin.getGuiConfig().getConfig().getString("settings.close_btn", "close")
        ))
            close = plugin.items.get(plugin.getGuiConfig().getConfig().getString("settings.close_btn", "close"));
        else close = new ItemStack(Material.BARRIER);

        inventory.setItem(49, close);


        if (plugin.items.containsKey(
                plugin.getGuiConfig().getConfig().getString("settings.close_btn", "close")
        ))
            drop = plugin.items.get(plugin.getGuiConfig().getConfig().getString("settings.drop_btn", "drop"));
        else drop = new ItemStack(Material.BARRIER);
        ItemMeta dropMeta = drop.getItemMeta();
        List<String> dropLore = new ArrayList<String>();


        if (plugin.itemUpgradesInfo.containsKey(item)) {
            ItemStack upgrade = drop.clone();
            ItemMeta upgradeMeta = upgrade.getItemMeta();
            List<String> upgradeLore = new ArrayList<>();
            upgradeMeta.setDisplayName(ChatColor.WHITE + "Улучшает:");
            for (EmpireItems.itemUpgradeClass iUg : plugin.itemUpgradesInfo.get(item)) {
                upgradeLore.add(ChatColor.GRAY + ItemUpgradeEvent.getStrAttr(iUg.attribute) + ":[" + iUg.min_add + ";" + iUg.max_add + "]");
            }
            upgradeMeta.setLore(upgradeLore);
            upgrade.setItemMeta(upgradeMeta);
            inventory.setItem(45, upgrade);
        }

        if (plugin.itemUpgradeCostDecreaser.containsKey(item)) {
            ItemStack upgradeCostDecrease = drop.clone();
            ItemMeta upgradeCostDecreaseMeta = upgradeCostDecrease.getItemMeta();
            upgradeCostDecreaseMeta.setDisplayName(ChatColor.WHITE + "Уменьшает стоимость апгрейда на " + plugin.itemUpgradeCostDecreaser.get(item));
            upgradeCostDecrease.setItemMeta(upgradeCostDecreaseMeta);
            inventory.setItem(47, upgradeCostDecrease);
        }
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
        if (playerMenuUtility.getPlayer().hasPermission("empireitems.give")) {
            ItemStack getItem;
            if (plugin.items.containsKey(
                    plugin.getGuiConfig().getConfig().getString("settings.give_btn")
            ))
                getItem = plugin.items.get(plugin.getGuiConfig().getConfig().getString("settings.give_btn"));
            else
                getItem = new ItemStack(Material.STONE);
            inventory.setItem(43, getItem);
        }
    }

    private List<String> getDrops(List<String> dropLore, HashMap<String, Drop[]> standartDrops) {
        HashMap<String, Drop[]> mDrops = new HashMap<>();

        for (String key : standartDrops.keySet()) {
            List<Drop> drops = new ArrayList<>();
            for (Drop s : standartDrops.get(key))
                if (item.equals(s.item)) {
                    drops.add(s);
                }
            if (drops.size() > 0) {
                Drop[] mDrop = new Drop[0];
                mDrop = drops.toArray(mDrop);
                mDrops.put(key, mDrop);
            }
        }

        for (String key : mDrops.keySet()) {
            dropLore.add(ChatColor.GRAY + key + ":\n");
            for (Drop s : mDrops.get(key))
                dropLore.add(ChatColor.GRAY + "От " + s.min_amount + " до " + s.max_amount + " с вер." + " " + s.chance + "%\n");
        }
        return dropLore;
    }

    private boolean IsDropped(HashMap<String, Drop[]> drop) {
        for (String key : drop.keySet())
            for (Drop s : drop.get(key))
                if (item.equals(s.item))
                    return true;


        return false;
    }
}
