package ru.empireprojekt.empireitems.ItemManager;

import com.sun.org.apache.xml.internal.utils.CharKey;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.sun.prism.paint.Color;
import org.bukkit.ChatColor;
import org.bukkit.permissions.Permission;
import org.bukkit.potion.PotionEffect;
import ru.empireprojekt.empireitems.events.InteractEvent;

import java.util.*;

public class GenericItem {
    public String itemId;
    public String display_name;
    public List<String> lore;
    public String permission;
    public String material;
    public Boolean enabled;
    public String model_path;
    public String texture_path;
    public List<?> pattern;
    public Map<Character, String> ingredients;
    public Map<String, Integer> enchantements;
    public List<InteractEvent> events;
    public List<mAttribute> attributes;
    public List<String> itemFlags;
    public Collection<PotionEffect> potionEffects;
    public int amount;
    public int customModelData;
    public int durability;



    public void PrintItem() {
        System.out.println(ChatColor.GREEN + "---------------------------Loaded item:---------------------------");
        System.out.println("display_name:" + display_name);
        System.out.println("lore:" + lore.toString());
        System.out.println("permission:" + permission);
        System.out.println("material:" + material);
        System.out.println("enabled:" + enabled);
        System.out.println("model_path:" + model_path);
        System.out.println("texture_path:" + texture_path);
        System.out.println("pattern:" + pattern);
        System.out.println("ingredients:" + ingredients);
        if (enchantements != null)
            System.out.println("enchantements:" + enchantements);
        if (attributes != null)
            for (mAttribute atr : attributes)
                atr.printAtr();
        if (events != null)
            for (InteractEvent ev : events)
                ev.PrintEvents();

        System.out.println("amount:" + amount);
        System.out.println("customModelData:" + customModelData);
        System.out.println(ChatColor.GREEN + "------------------------------------------------------------------");
    }
}
