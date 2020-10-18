package ru.empireprojekt.empireitems.ItemManager;

import com.sun.org.apache.xml.internal.utils.CharKey;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.sun.prism.paint.Color;
import org.bukkit.ChatColor;
import org.bukkit.permissions.Permission;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericItem {
   public String display_name;
   public List<String> lore;
   public String permission;
   public Boolean enabled;
   public String model_path;
   public String texture_path;
   public List<?> pattern;
   public Map<Character,String> ingredients;
   public Map<String,Integer> enchantements;
   public int amount;
   public int customModelData;


    public void PrintItem(){
        System.out.println(ChatColor.GREEN+"---------------------------Loaded item:---------------------------");
        System.out.println("display_name:" + display_name);
        System.out.println("lore:" + lore.toString());
        System.out.println("permission:" + permission);
        System.out.println("enabled:" + enabled);
        System.out.println("model_path:" + model_path);
        System.out.println("texture_path:" + texture_path);
        System.out.println("pattern:" + pattern);
        System.out.println("ingredients:" + ingredients);
        System.out.println("enchantements:" + enchantements);
        System.out.println("amount:" + amount);
        System.out.println("customModelData:" + customModelData);
        System.out.println(ChatColor.GREEN+"------------------------------------------------------------------");
    }
}
