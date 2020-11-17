package ru.empireprojekt.empireitems.ItemManager;

import com.google.gson.*;
import jdk.nashorn.internal.parser.JSONParser;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Item;
import org.json.simple.JSONObject;
import ru.empireprojekt.empireitems.EmpireItems;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ItemManager {
    public class mItem {
        String texture_path = null;
        String model_path = null;
        String item_name = null;
        String material = null;
        String namespace = "empire_items";
        int custom_model_data = 1;

        public mItem(String namespace, String texture_path, String model_path, String item_name, String material, int custom_model_data) {
            this.namespace = namespace;
            this.texture_path = texture_path;
            this.model_path = model_path;
            this.item_name = item_name;

            this.material = material;
            this.custom_model_data = custom_model_data;
        }
    }

    List<mItem> items;
    EmpireItems plugin;
    public List<String > GetNames(){
        List<String> mArgs=new ArrayList<String>();
        for (mItem item: items)
            mArgs.add(item.item_name);
        return mArgs;
    }

    public ItemManager(EmpireItems plugin) {
        this.plugin = plugin;
        items = new ArrayList<mItem>();
    }

    public void AddItem(String namespace, String texture_path, String model_path, String item_name, String material, int custom_model_data) {
        items.add(new mItem(namespace, texture_path, model_path, item_name, material, custom_model_data));
    }

    public void GenerateMinecraftModels() {
        JsonParser jsonParser = new JsonParser();
        for (mItem item : items) {

            try {
                FileReader reader = new FileReader(plugin.getDataFolder() + "\\pack\\assets\\minecraft\\models\\item\\" + item.material.toLowerCase() + ".json");
                JsonObject jsonFile = (JsonObject) jsonParser.parse(reader);
                if (jsonFile.has("overrides"))
                    jsonFile.remove("overrides");
                FileWriter file = new FileWriter(plugin.getDataFolder() + "\\pack\\assets\\minecraft\\models\\item\\" + item.material.toLowerCase() + ".json");
                file.write(jsonFile.toString());
                file.close();
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println(ChatColor.RED + "Не удалось очистить файл: " + item.material + ".json");
            }
        }


        System.out.println(ChatColor.YELLOW + "Generating Models");
        for (mItem item : items) {
            try {
                //Generating minecraft models
                System.out.println(plugin.getDataFolder() + "\\pack\\assets\\minecraft\\models\\item\\" + item.material.toLowerCase() + ".json");
                //Уже существует
                FileReader reader = new FileReader(plugin.getDataFolder() + "\\pack\\assets\\minecraft\\models\\item\\" + item.material.toLowerCase() + ".json");

                JsonObject jsonFile = (JsonObject) jsonParser.parse(reader);

                JsonArray overrides;
                if (jsonFile.has("overrides"))
                    overrides = jsonFile.getAsJsonArray("overrides");
                else
                    overrides = new JsonArray();
                String parent = jsonFile.get("parent").toString().replaceAll("\"","");
                JsonObject itemObj = new JsonObject();
                JsonObject predicate = new JsonObject();
                predicate.addProperty("custom_model_data", item.custom_model_data);
                itemObj.add("predicate", predicate);
                if (item.model_path!=null)
                    itemObj.addProperty("model", item.namespace + ":"+item.model_path);
                else
                    itemObj.addProperty("model", item.namespace + ":auto_generated/" + item.item_name);
                overrides.add(itemObj);
                reader.close();
                if (jsonFile.has("overrides"))
                    jsonFile.remove("overrides");
                jsonFile.add("overrides", overrides);
                FileWriter file = new FileWriter(plugin.getDataFolder() + "\\pack\\assets\\minecraft\\models\\item\\" + item.material.toLowerCase() + ".json");
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                file.write(gson.toJson(jsonFile));
                file.close();

                System.out.println("Создание файла: "+plugin.getDataFolder() + "\\pack\\assets\\" + item.namespace + "\\models\\auto_generated\\" + item.item_name + ".json");
                File mFile = new File(plugin.getDataFolder() + "\\pack\\assets\\" + item.namespace + "\\models\\auto_generated\\" + item.item_name + ".json");
                if (!mFile.getParentFile().exists() && !mFile.getParentFile().mkdirs())
                    System.out.println(ChatColor.RED+"Не удалось создать файл");

                file = new FileWriter(plugin.getDataFolder() + "\\pack\\assets\\" + item.namespace + "\\models\\auto_generated\\" + item.item_name + ".json");
                jsonFile = new JsonObject();
                jsonFile.addProperty("parent", parent);
                itemObj = new JsonObject();
                itemObj.addProperty("layer0",item.namespace+":"+item.texture_path);
                jsonFile.add("textures",itemObj);
                file.write(gson.toJson(jsonFile));
                file.close();

            } catch (FileNotFoundException e) {
                //e.printStackTrace();
                System.out.println(ChatColor.YELLOW + "Не найден файл: " + item.material + ".json");
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println(ChatColor.YELLOW + "Возникла ошибка при выполении файла: " + item.material + ".json");
            }
        }
    }

    public void print() {
        System.out.println(ChatColor.GREEN + "--------------------------ItemManager--------------------------");
        for (mItem item : items) {

            System.out.println("namespace: " + item.namespace);
            System.out.println("texture_path: " + item.texture_path);
            System.out.println("model_path: " + item.model_path);
            System.out.println("item_name: " + item.item_name);
            System.out.println("material: " + item.material);
            System.out.println("custom_model_data: " + item.custom_model_data);
            System.out.println(ChatColor.GREEN + "---------------------------------------------------------------");
        }
    }
//    public void Generate_custom_model_data(){
//        int d=0;
//        for (mItem item : items) {
//            item.custom_model_data=d++;
//        }
//    }
}
