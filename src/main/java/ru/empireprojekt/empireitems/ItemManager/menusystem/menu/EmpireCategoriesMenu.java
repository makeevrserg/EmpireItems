package ru.empireprojekt.empireitems.ItemManager.menusystem.menu;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.empireprojekt.empireitems.EmpireItems;
import ru.empireprojekt.empireitems.ItemManager.menusystem.MenuItems;
import ru.empireprojekt.empireitems.ItemManager.menusystem.PaginatedMenu;
import ru.empireprojekt.empireitems.ItemManager.menusystem.PlayerMenuUtility;

public class EmpireCategoriesMenu extends PaginatedMenu {
    EmpireItems plugin;
    int maxPages = 0;

    public EmpireCategoriesMenu(PlayerMenuUtility playerMenuUtility, EmpireItems plugin) {
        super(playerMenuUtility);
        this.plugin = plugin;
        maxPages = getMaxPages();
    }

    public String getMenuName() {
        return plugin.generic_item.getGuiConfig().getString("settings.categories_text", "Категории");
    }

    public int getSlots() {
        return 54;
    }

    public void handleMenu(InventoryClickEvent e) {
        if (e.getCurrentItem() != null) {
            if (e.getSlot() != 45 && e.getSlot() != 49 && e.getSlot() != 53)
                new EmpireCategoryMenu(playerMenuUtility, plugin, e.getSlot()).open();
            if (e.getSlot() == 45) {
                if (page == 0) {
                    playerMenuUtility.getOwner().sendMessage(ChatColor.YELLOW + "Вы на первой странице");
                    return;
                }
                page -= 1;
                inventory.clear();
                setMenuItems();
            } else if (e.getSlot() == 49)
                e.getWhoClicked().closeInventory();
            else if (e.getSlot() == 53) {
                if (page >= maxPages) {
                    playerMenuUtility.getOwner().sendMessage(ChatColor.YELLOW + "Вы на последней странице");
                    return;
                }
                page += 1;
                inventory.clear();
                setMenuItems();
            }
        }else
            return;

    }

    private int getMaxPages() {
        int size = plugin.menuItems.size();
        int mP = size / maxItemsPerPage;
        mP += (size % maxItemsPerPage > 0) ? 1 : 0;
        return mP - 1;
    }

    public void setMenuItems() {
        addManageButtons(plugin);

        for (int i = 0; i < super.maxItemsPerPage; ++i) {
            index = super.maxItemsPerPage * page + i;
            if (index < plugin.menuItems.size() && plugin.menuItems.get(index) != null) {
                MenuItems menuItem = plugin.menuItems.get(index);
                ItemStack item;
                ItemMeta itemMeta;
                if (Material.getMaterial(menuItem.categoryIcon) != null)
                    item = new ItemStack(Material.getMaterial(menuItem.categoryIcon));
                else if (plugin.items.containsKey(menuItem.categoryIcon))
                    item = plugin.items.get(menuItem.categoryIcon).clone();
                else {
                    System.out.println(ChatColor.YELLOW + "Предмет не найден:" + menuItem.categoryIcon);
                    item = new ItemStack(Material.STONE);
                }
                itemMeta = item.getItemMeta().clone();
                menuItem.categoryName=plugin.HEXPattern(menuItem.categoryName);
                itemMeta.setDisplayName(menuItem.categoryName);
                plugin.HEXPattern(menuItem.categoryLore);
                itemMeta.setLore(menuItem.categoryLore);

                itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                item.setItemMeta(itemMeta);

                inventory.setItem(i, item);
            }
        }
    }
}
