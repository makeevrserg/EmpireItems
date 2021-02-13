package ru.empireprojekt.empireitems.ItemManager.menusystem;

import java.util.List;

public class MenuItems {
    public String categoryName;
    public String categoryIcon;
    public String categoryTitle;
    public List<String> categoryLore;
    public List<String> categoryItems;

    public MenuItems(String categoryTitle, String categoryName, String categoryIcon, List<String> categoryLore, String categoryPermission, List<String> categoryItems) {
        this.categoryTitle = categoryTitle;
        this.categoryName = categoryName;
        this.categoryIcon = categoryIcon;
        this.categoryLore = categoryLore;
        //this.categoryPermission = categoryPermission;
        this.categoryItems = categoryItems;
    }
}
