package ru.empireprojekt.empireitems.ItemManager.menusystem;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.empireprojekt.empireitems.EmpireItems;

public abstract class PaginatedMenu extends Menu {
    public PaginatedMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    protected int page = 0;
    protected int maxItemsPerPage = 45;
    protected int index = 0;

    public void addManageButtons(EmpireItems plugin) {
        ItemStack left;
        String btn = plugin.generic_item.getGuiConfig().getString("settings.prev_btn");
        if (btn == null || !plugin.items.containsKey(btn)) {
            left = new ItemStack(Material.PAPER, 1);
            ItemMeta leftMeta = left.getItemMeta();
            leftMeta.setDisplayName(ChatColor.GREEN + "<- Пред. страница");
            left.setItemMeta(leftMeta);
        } else
            left = plugin.items.get(btn);

        inventory.setItem(45, left);
        ItemStack back;
        btn = plugin.generic_item.getGuiConfig().getString("settings.back_btn");
        if (btn == null || !plugin.items.containsKey(btn)) {
            back = new ItemStack(Material.PAPER, 1);
            ItemMeta backMeta = back.getItemMeta();
            backMeta.setDisplayName(ChatColor.GREEN + "Назад");
            back.setItemMeta(backMeta);
        } else
            back = plugin.items.get(btn);

        inventory.setItem(49, back);

        ItemStack right;
        btn = plugin.generic_item.getGuiConfig().getString("settings.next_btn");
        if (btn == null || !plugin.items.containsKey(btn)) {
            right = new ItemStack(Material.PAPER, 1);
            ItemMeta rightMeta = right.getItemMeta();
            rightMeta.setDisplayName(ChatColor.GREEN + "След. страница ->");
            right.setItemMeta(rightMeta);
        } else
            right = plugin.items.get(btn);

        inventory.setItem(53, right);

    }
}
