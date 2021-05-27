package ru.empireprojekt.empireitems;

import org.bukkit.configuration.ConfigurationSection;
import ru.empireprojekt.empireitems.ItemManager.menusystem.MenuItems;
import ru.empireprojekt.empireitems.files.DataManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomUISettings {

    List<InterfaceItem> emojiItems;
    List<InterfaceItem> guiItems;

    HashMap<String, String> emojis;
    HashMap<String, String> guis;
    HashMap<String, String> offsetsUi;
    DataManager guiConfig;
    EmpireItems plugin;
    public CustomUISettings(EmpireItems plugin){
        this.plugin= plugin;
        this.guiConfig = new DataManager("gui.yml", plugin);
        guiItems = generateInterfaceItems(getGuiConfig().getConfig().getConfigurationSection("interface"));
        emojiItems = generateInterfaceItems(getGuiConfig().getConfig().getConfigurationSection("emoji"));
        emojis = GenerateUIElements(emojiItems);
        guis = GenerateUIElements(guiItems);
        offsetsUi = new HashMap<>();
        for (String key:guiConfig.getConfig().getConfigurationSection("offsets").getKeys(false))
            offsetsUi.put(":"+key+":",
                    guiConfig.getConfig().getConfigurationSection("offsets").getString(key));
    }



    public HashMap<String, String> getEmojis() {
        return emojis;
    }

    public class InterfaceItem {
        public String name;
        public String path;
        public String namespace;
        public int size;
        public int offset;
        public String chars;

        public InterfaceItem(String name, String path, String namespace, int size, int offset, String chars) {
            this.name = name;
            this.path = path;
            this.namespace = namespace;
            this.size = size;
            this.offset = offset;
            this.chars = chars;
        }

        public boolean hasNull() {
            if (path == null) return true;
            else if (namespace == null) return true;
            else if (chars == null) return true;
            else if (size == -1) return true;
            else return offset == -1;
        }

    }

    public List<InterfaceItem> getGuiItems() {
        return guiItems;
    }

    public List<InterfaceItem> getEmojiItems() {
        return emojiItems;
    }

    private List<InterfaceItem> generateInterfaceItems(ConfigurationSection interfaceSection) {
        ArrayList<InterfaceItem> interfaceItems = new ArrayList<>();
        for (String key : interfaceSection.getKeys(false)) {
            ConfigurationSection sect = interfaceSection.getConfigurationSection(key);
            if (sect == null)
                continue;
            InterfaceItem interfaceItem = new InterfaceItem(
                    key,
                    sect.getString("path", null),
                    sect.getString("namespace", null),
                    sect.getInt("size", 10),
                    sect.getInt("offset", 8),
                    sect.getString("chars", null)
            );
            interfaceItems.add(interfaceItem);
        }
        return interfaceItems;
    }

    private HashMap<String, String> GenerateUIElements(List<InterfaceItem> items) {
        HashMap<String, String> map = new HashMap<>();
        for (InterfaceItem item : items) {
            char ch = (char) Integer.parseInt(item.chars.substring(2), 16);
            map.put(":" + item.name + ":", String.valueOf(ch));
        }
        return map;

    }

    public DataManager getGuiConfig() {
        return guiConfig;
    }


}
