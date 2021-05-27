package ru.empireprojekt.empireitems;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmpireConstants {

    public String PLUGIN_MESSAGE = ChatColor.AQUA + "[EmpireItems] " + ChatColor.GREEN;

    public NamespacedKey empireID;
    public NamespacedKey durabilityMechanicNamespace;
    public NamespacedKey maxCustomDurability;
    public NamespacedKey ITEM_UPGRADE_COUNT;//Подсчёт улучшений предмета
    public NamespacedKey GENERIC_ARMOR;
    public NamespacedKey GENERIC_ARMOR_TOUGHNESS;
    public NamespacedKey GENERIC_KNOCKBACK_RESISTANCE;
    public NamespacedKey GENERIC_LUCK;
    public NamespacedKey GENERIC_MAX_HEALTH;
    public NamespacedKey GENERIC_MOVEMENT_SPEED;
    public NamespacedKey GENERIC_ATTACK_DAMAGE;
    public NamespacedKey GENERIC_ATTACK_KNOCKBACK;
    public NamespacedKey GENERIC_ATTACK_SPEED;
    public NamespacedKey itemExplosionNamespace;
    public NamespacedKey itemHammer;
    public NamespacedKey lavaWalker;
    public NamespacedKey vampirism;
    public EmpireItems plugin;

    EmpireConstants(EmpireItems plugin) {
        this.plugin = plugin;
        GENERIC_ATTACK_DAMAGE = new NamespacedKey(plugin, "GENERIC_ATTACK_DAMAGE");
        GENERIC_ATTACK_KNOCKBACK = new NamespacedKey(plugin, "GENERIC_ATTACK_KNOCKBACK");
        GENERIC_ATTACK_SPEED = new NamespacedKey(plugin, "GENERIC_ATTACK_SPEED");

        GENERIC_MOVEMENT_SPEED = new NamespacedKey(plugin, "GENERIC_MOVEMENT_SPEED");
        GENERIC_MAX_HEALTH = new NamespacedKey(plugin, "GENERIC_MAX_HEALTH");
        GENERIC_LUCK = new NamespacedKey(plugin, "GENERIC_LUCK");
        GENERIC_KNOCKBACK_RESISTANCE = new NamespacedKey(plugin, "GENERIC_KNOCKBACK_RESISTANCE");
        GENERIC_ARMOR_TOUGHNESS = new NamespacedKey(plugin, "GENERIC_ARMOR_TOUGHNESS");
        GENERIC_ARMOR = new NamespacedKey(plugin, "GENERIC_ARMOR");

        empireID = new NamespacedKey(plugin, "id");
        durabilityMechanicNamespace = new NamespacedKey(plugin, "durability");
        maxCustomDurability = new NamespacedKey(plugin, "maxCustomDurability");
        ITEM_UPGRADE_COUNT = new NamespacedKey(plugin, "ITEM_UPGRADE_COUNT");

        itemHammer = new NamespacedKey(plugin, "itemHammer");
        lavaWalker = new NamespacedKey(plugin, "lavaWalker");
        vampirism = new NamespacedKey(plugin, "vampirism");


        itemExplosionNamespace = new NamespacedKey(plugin, "onHitGroundExplosion");

    }

    private final Pattern emojiPattern = Pattern.compile(":([a-zA-Z0-9_]*):");


    public final Pattern getEmojiPattern() {
        return emojiPattern;
    }

    public String GetEmoji(String msg) {
        return StringToUnicode(msg,plugin.getCustomUISettings().emojis);
    }
    public String GetUi(String msg) {
        HashMap<String,String> map = new HashMap<>();
        map.putAll(plugin.getCustomUISettings().guis);
        map.putAll(plugin.getCustomUISettings().emojis);
        map.putAll(plugin.getCustomUISettings().offsetsUi);
        return StringToUnicode(msg,map);
    }
    public String StringToUnicode(String msg, HashMap<String, String> map) {
        Matcher matcher = emojiPattern.matcher(msg);
        while (matcher.find()) {
            String emoji = msg.substring(matcher.start(), matcher.end());
            String toReplace = map.get(emoji);
            if (toReplace == null)
                toReplace = emoji.replaceAll(":", "");

            msg = msg.replace(emoji, toReplace + "");
            matcher = emojiPattern.matcher(msg);
        }
        msg = HEXPattern(msg);
        return msg;
    }

    private final Pattern hexPattern = Pattern.compile("#[a-fA-F0-9]{6}|&#[a-fA-F0-9]{6}");

    public final Pattern getHexPattern() {
        return hexPattern;
    }

    public List<String> HEXPattern(List<String> list) {
        for (int i = 0; i < list.size(); ++i)
            list.set(i, HEXPattern(list.get(i)));
        return list;
    }

    //Создает цветное сообщение по паттерну #FFFFFF или &2
    public String HEXPattern(String line) {
        Matcher match = hexPattern.matcher(line);
        while (match.find()) {
            String color = line.substring(match.start(), match.end());
            line = line.replace(color, net.md_5.bungee.api.ChatColor.of(
                    color.startsWith("&")?color.substring(1):color
            ) + "");
            match = hexPattern.matcher(line);
        }
        return ChatColor.translateAlternateColorCodes('&', line);
    }

    public void onDestroy() {

        empireID = null;
        durabilityMechanicNamespace = null;
        maxCustomDurability = null;
        ITEM_UPGRADE_COUNT = null;
        itemHammer = null;
        lavaWalker = null;
        GENERIC_ARMOR = null;
        GENERIC_ARMOR_TOUGHNESS = null;
        GENERIC_KNOCKBACK_RESISTANCE = null;
        GENERIC_LUCK = null;
        GENERIC_MAX_HEALTH = null;
        GENERIC_MOVEMENT_SPEED = null;
        GENERIC_ATTACK_DAMAGE = null;
        GENERIC_ATTACK_KNOCKBACK = null;
        GENERIC_ATTACK_SPEED = null;
        vampirism = null;
        itemExplosionNamespace = null;
    }

}
