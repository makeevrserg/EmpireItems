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

    public List<String> GetNames() {
        List<String> mArgs = new ArrayList<String>();
        for (mItem item : items)
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


    private JsonObject GetBowObject(int pulling, String model, String namespace, int custom_model_data) {
        JsonObject bow;
        JsonObject predicate;
        predicate = new JsonObject();
        bow = new JsonObject();
        predicate.addProperty("pulling", pulling);
        if (model.contains("_1"))
            predicate.addProperty("pull", 0.65);
        else if (model.contains("_2"))
            predicate.addProperty("pull", 0.9);
        if (custom_model_data > 0)
            predicate.addProperty("custom_model_data", custom_model_data);
        bow.add("predicate", predicate);
        if (namespace.length() > 0)
            bow.addProperty("model", namespace + ":auto_generated/" + model);
        else
            bow.addProperty("model", model);
        return bow;
    }

    private JsonObject GetShieldObject(int blocking, String model, int customModelData) {
        JsonObject shield;
        JsonObject predicate;
        predicate = new JsonObject();
        shield = new JsonObject();
        shield.addProperty("model", model);
        predicate.addProperty("blocking", blocking);
        if (customModelData > 0)
            predicate.addProperty("custom_model_data", customModelData);
        shield.add("predicate", predicate);
        return shield;
    }

    private JsonObject GetGenericObject(String modelPath, int customModelData) {
        JsonObject itemObj;
        JsonObject predicate;
        predicate = new JsonObject();
        itemObj = new JsonObject();
        predicate.addProperty("custom_model_data", customModelData);
        itemObj.addProperty("model", modelPath);
        itemObj.add("predicate", predicate);
        return itemObj;
    }

    private JsonObject auto_generate(String parent, String namespace, String layerPath, String modelName) throws IOException {
        JsonObject itemObj = new JsonObject();
        itemObj.addProperty("parent", parent);
        JsonObject layer = new JsonObject();
        layer.addProperty("layer0", namespace + ":" + layerPath);
        itemObj.add("textures", layer);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileWriter file = new FileWriter(plugin.getDataFolder() + "\\pack\\assets\\" + namespace + "\\models\\auto_generated\\" + modelName + ".json");
        file.write(gson.toJson(itemObj));
        file.close();

        return itemObj;
    }

    public void GenerateMinecraftModels() {
        JsonParser jsonParser = new JsonParser();
        //Чистим файыл
        for (mItem item : items) {
            try {
                FileReader reader = new FileReader(plugin.getDataFolder() + "\\pack\\assets\\minecraft\\models\\item\\" + item.material.toLowerCase() + ".json");
                JsonObject jsonFile = (JsonObject) jsonParser.parse(reader);
                JsonArray overrides = new JsonArray();

                if (jsonFile.has("overrides"))
                    jsonFile.remove("overrides");

                if (item.material.toLowerCase().equals("shield")) {
                    GetShieldObject(0, "item/shield", 0);
                    GetShieldObject(1, "item/shield_blocking", 0);
                    jsonFile.add("overrides", overrides);
                }
                if (item.material.toLowerCase().equals("bow")) {
                    overrides.add(GetBowObject(0, "item/bow", "", 0));
                    overrides.add(GetBowObject(1, "item/bow_pulling_0", "", 0));
                    overrides.add(GetBowObject(1, "item/bow_pulling_1", "", 0));
                    overrides.add(GetBowObject(1, "item/bow_pulling_2", "", 0));
                    jsonFile.add("overrides", overrides);
                }


                FileWriter file = new FileWriter(plugin.getDataFolder() + "\\pack\\assets\\minecraft\\models\\item\\" + item.material.toLowerCase() + ".json");

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                file.write(gson.toJson(jsonFile));
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
                //Открыли существующий файл
                FileReader reader = new FileReader(plugin.getDataFolder() + "\\pack\\assets\\minecraft\\models\\item\\" + item.material.toLowerCase() + ".json");
                JsonObject jsonFile = (JsonObject) jsonParser.parse(reader);
                reader.close();
                if (item.material.toLowerCase().equals("bow")) {
                    JsonArray overrides = jsonFile.getAsJsonArray("overrides");
                    overrides.add(GetBowObject(0, item.item_name, item.namespace, item.custom_model_data));
                    overrides.add(GetBowObject(1, item.item_name + "_0", item.namespace, item.custom_model_data));
                    overrides.add(GetBowObject(1, item.item_name + "_1", item.namespace, item.custom_model_data));
                    overrides.add(GetBowObject(1, item.item_name + "_2", item.namespace, item.custom_model_data));
                    jsonFile.add("overrides", overrides);

                    //auto_generate
                    auto_generate(jsonFile.get("parent").toString().replaceAll("\"", ""),
                            item.namespace,
                            item.texture_path.replace(".png", ""),
                            item.item_name
                    );
                    auto_generate(jsonFile.get("parent").toString().replaceAll("\"", ""),
                            item.namespace,
                            item.texture_path.replace(".png", "") + "_0",
                            item.item_name + "_0"
                    );
                    auto_generate(jsonFile.get("parent").toString().replaceAll("\"", ""),
                            item.namespace,
                            item.texture_path.replace(".png", "") + "_1",
                            item.item_name + "_1"
                    );
                    auto_generate(jsonFile.get("parent").toString().replaceAll("\"", ""),
                            item.namespace,
                            item.texture_path.replace(".png", "") + "_2",
                            item.item_name + "_2"
                    );

                } else if (item.material.toLowerCase().equals("shield")) {
                    JsonArray overrides = jsonFile.getAsJsonArray("overrides");
                    overrides.add(GetShieldObject(0, item.namespace + ":item/" + item.item_name, item.custom_model_data));
                    overrides.add(GetShieldObject(1, item.namespace + ":item/" + item.item_name, item.custom_model_data));
                    jsonFile.add("overrides", overrides);
                } else {
                    JsonArray overrides;
                    if (jsonFile.has("overrides"))
                        overrides = jsonFile.getAsJsonArray("overrides");
                    else
                        overrides = new JsonArray();
                    System.out.println(item.material + ";" + item.item_name);
                    if (item.model_path != null)
                        overrides.add(GetGenericObject(item.namespace + ":" + item.model_path, item.custom_model_data));
                    else
                        overrides.add(GetGenericObject(item.namespace + ":auto_generated/" + item.item_name, item.custom_model_data));
                    jsonFile.add("overrides", overrides);
                    auto_generate(jsonFile.get("parent").toString().replaceAll("\"", ""), item.namespace, item.texture_path, item.item_name);
                }

                FileWriter file = new FileWriter(plugin.getDataFolder() + "\\pack\\assets\\minecraft\\models\\item\\" + item.material.toLowerCase() + ".json");
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
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
}
