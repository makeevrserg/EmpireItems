package ru.empireprojekt.empireitems.events;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import ru.empireprojekt.empireitems.EmpireConstants;
import ru.empireprojekt.empireitems.EmpireItems;
import ru.empireprojekt.empireitems.ItemUpgradeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemUpgradeEvent implements Listener {

    EmpireItems plugin;
    EmpireConstants CONSTANTS;

    public ItemUpgradeEvent(EmpireItems plugin) {
        this.plugin = plugin;
        CONSTANTS = plugin.CONSTANTS;

    }


    private void AddAmountToContainer(String atrName, String atrToCompaer, NamespacedKey namespacedKey, PersistentDataContainer oldItemContainer, PersistentDataContainer resultContainer, double amount) {
        if (atrName.equalsIgnoreCase(atrToCompaer)) {
            Double oldValue = oldItemContainer.get(namespacedKey, PersistentDataType.DOUBLE);
            amount += (oldValue == null) ? 0 : oldValue;
            amount = Math.round(amount * 10.0) / 10.0;
            resultContainer.set(namespacedKey, PersistentDataType.DOUBLE, amount);
        }
    }

    private String getAttrColor(double amount) {
        if (amount < 1)
            return "#d4311c";
        else if (amount < 5)
            return "#d4781c";
        else if (amount < 10)
            return "#d4d41c";
        else if (amount < 15)
            return "#7ed41c";
        else if (amount < 20)
            return "#2ed41c";
        else
            return "#1ccbd4";
    }

    public static String getStrAttr(String str) {
        if (str.equalsIgnoreCase("GENERIC_ATTACK_SPEED"))
            return "Скорость атаки";
        else if (str.equalsIgnoreCase("GENERIC_ATTACK_KNOCKBACK"))
            return "Откидывание";
        else if (str.equalsIgnoreCase("GENERIC_ATTACK_DAMAGE"))
            return "Урон";
        else if (str.equalsIgnoreCase("GENERIC_ARMOR"))
            return "Броня";
        else if (str.equalsIgnoreCase("GENERIC_ARMOR_TOUGHNESS"))
            return "Прочность брони";
        else if (str.equalsIgnoreCase("GENERIC_KNOCKBACK_RESISTANCE"))
            return "Сопротивление откидыванию";
        else if (str.equalsIgnoreCase("GENERIC_MAX_HEALTH"))
            return "Здоровье";
        else if (str.equalsIgnoreCase("GENERIC_LUCK"))
            return "Удача";
        else if (str.equalsIgnoreCase("GENERIC_MOVEMENT_SPEED"))
            return "Скорость";
        return "";
    }

    private void ReplaceLore(List<String> fromReplace, String toReplace, String key) {
        for (int i = 0; i < fromReplace.size(); ++i)
            if (fromReplace.get(i).contains(key))
                //noinspection SuspiciousListRemoveInLoop
                fromReplace.remove(i);

        fromReplace.add(toReplace);


    }


    private void CheckContainer(List<String> lore, PersistentDataContainer container, NamespacedKey namespacedKey, String attrName) {
        if (!container.has(namespacedKey, PersistentDataType.DOUBLE))
            return;
        String attr = getStrAttr(attrName);
        @SuppressWarnings("ConstantConditions")
        double amount = container.get(namespacedKey, PersistentDataType.DOUBLE);

        String color = getAttrColor(amount);
        if (amount < 0)
            ReplaceLore(lore, plugin.CONSTANTS.HEXPattern(color + attr + ' ' + amount), attr);
        else
            ReplaceLore(lore, plugin.CONSTANTS.HEXPattern(color + attr + " +" + amount), attr);

    }

    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent e) {
        if (e.getInventory() instanceof AnvilInventory) {
            InventoryView view = e.getView();
            int rawSlot = e.getRawSlot();
            if (rawSlot == view.convertSlot(rawSlot)) {
                if (rawSlot == 2) {
                    ItemStack item = e.getCurrentItem();
                    if (item != null) {
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            PersistentDataContainer container = meta.getPersistentDataContainer();
                            if (container.has(CONSTANTS.empireID, PersistentDataType.STRING)) {
                                List<String> lore = meta.getLore();
                                if (lore == null)
                                    lore = new ArrayList<>();
                                CheckContainer(lore, container, CONSTANTS.GENERIC_ATTACK_DAMAGE, "GENERIC_ATTACK_DAMAGE");
                                CheckContainer(lore, container, CONSTANTS.GENERIC_ATTACK_SPEED, "GENERIC_ATTACK_SPEED");
                                CheckContainer(lore, container, CONSTANTS.GENERIC_ATTACK_KNOCKBACK, "GENERIC_ATTACK_KNOCKBACK");
                                CheckContainer(lore, container, CONSTANTS.GENERIC_ARMOR, "GENERIC_ARMOR");
                                CheckContainer(lore, container, CONSTANTS.GENERIC_ARMOR_TOUGHNESS, "GENERIC_ARMOR_TOUGHNESS");
                                CheckContainer(lore, container, CONSTANTS.GENERIC_KNOCKBACK_RESISTANCE, "GENERIC_KNOCKBACK_RESISTANCE");
                                CheckContainer(lore, container, CONSTANTS.GENERIC_LUCK, "GENERIC_LUCK");
                                CheckContainer(lore, container, CONSTANTS.GENERIC_MAX_HEALTH, "GENERIC_MAX_HEALTH");
                                CheckContainer(lore, container, CONSTANTS.GENERIC_MOVEMENT_SPEED, "GENERIC_MOVEMENT_SPEED");


                                meta.setLore(lore);
                                item.setItemMeta(meta);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void anvilEvent(PrepareAnvilEvent e) {

        if (plugin.mSettings.isUpgradeEnabled) {
            ItemStack itemBefore = e.getInventory().getItem(0);
            ItemStack ingredient = e.getInventory().getItem(1);

            if (itemBefore != null && ingredient != null) {
                ItemMeta itemMetaBefore = itemBefore.getItemMeta();
                if (itemMetaBefore != null && e.getInventory().getSize() > 1) {
                    ItemMeta ingrMeta = ingredient.getItemMeta();
                    //Check on Upgrade
                    if (ingrMeta != null) {
                        PersistentDataContainer oldItemContainer = itemMetaBefore.getPersistentDataContainer();
                        PersistentDataContainer ingridientContainer = ingrMeta.getPersistentDataContainer();
                        int upgradeCount = ingredient.getAmount();
                        if (oldItemContainer.has(CONSTANTS.ITEM_UPGRADE_COUNT, PersistentDataType.INTEGER))
                            //noinspection ConstantConditions
                            upgradeCount += oldItemContainer.get(CONSTANTS.ITEM_UPGRADE_COUNT, PersistentDataType.INTEGER);


                        if (oldItemContainer.has(CONSTANTS.empireID, PersistentDataType.STRING) && ingridientContainer.has(CONSTANTS.empireID, PersistentDataType.STRING)) {
                            String ingrId = ingridientContainer.get(CONSTANTS.empireID, PersistentDataType.STRING);
                            ItemStack result = itemBefore.clone();
                            ItemMeta resultMeta = result.getItemMeta();
                            assert resultMeta != null;
                            PersistentDataContainer resultContainer = resultMeta.getPersistentDataContainer();
                            //Если ингридиент снижает стоимость апгрейда
                            if (plugin.getItemUpgradeManager().getItemUpgradeCostDecreaser().containsKey(ingrId)) {
                                if (oldItemContainer.has(CONSTANTS.ITEM_UPGRADE_COUNT, PersistentDataType.INTEGER))
                                    //noinspection ConstantConditions
                                    upgradeCount -= oldItemContainer.get(CONSTANTS.ITEM_UPGRADE_COUNT, PersistentDataType.INTEGER);
                                int upgradeDecrease = (int) (plugin.getItemUpgradeManager().getItemUpgradeCostDecreaser().get(ingrId) * (double) ingredient.getAmount());
                                upgradeCount -= upgradeDecrease;
                                if (upgradeCount <= 0)
                                    upgradeCount = 1;
                                resultContainer.set(CONSTANTS.ITEM_UPGRADE_COUNT, PersistentDataType.INTEGER, upgradeCount);
                                result.setItemMeta(resultMeta);
                                e.setResult(result);
                                e.getInventory().setRepairCost((ingredient.getAmount()));
                                plugin.getServer().getScheduler().runTask(plugin, () -> e.getInventory().setRepairCost(ingredient.getAmount()));


                            } else if (plugin.getItemUpgradeManager().itemUpgradesInfo.containsKey(ingridientContainer.get(CONSTANTS.empireID, PersistentDataType.STRING))) {
                                resultContainer.set(CONSTANTS.ITEM_UPGRADE_COUNT, PersistentDataType.INTEGER, upgradeCount);
                                upgradeCount -= ingredient.getAmount();
                                for (ItemUpgradeManager.itemUpgradeClass iUpg : plugin.getItemUpgradeManager().itemUpgradesInfo.get(ingrId)) {
                                    double amount = (iUpg.min_add + (Math.random() * (iUpg.max_add - iUpg.min_add))) * ingredient.getAmount();
                                    if (itemBefore.getType().toString().toLowerCase().contains("helmet"))
                                        iUpg.slot = EquipmentSlot.HEAD.name();
                                    if (itemBefore.getType().toString().toLowerCase().contains("leggings"))
                                        iUpg.slot = EquipmentSlot.LEGS.name();
                                    if (itemBefore.getType().toString().toLowerCase().contains("chestplate"))
                                        iUpg.slot = EquipmentSlot.CHEST.name();
                                    if (itemBefore.getType().toString().toLowerCase().contains("boots"))
                                        iUpg.slot = EquipmentSlot.FEET.name();

                                    if (itemBefore.getType().equals(Material.BOW))
                                        return;

                                    if (itemBefore.getType().toString().toLowerCase().contains("sword") || itemBefore.getType().toString().toLowerCase().contains("axe")) {
                                        AddAmountToContainer(iUpg.attribute, "GENERIC_ATTACK_DAMAGE", CONSTANTS.GENERIC_ATTACK_DAMAGE, oldItemContainer, resultContainer, amount);
                                        AddAmountToContainer(iUpg.attribute, "GENERIC_ATTACK_SPEED", CONSTANTS.GENERIC_ATTACK_SPEED, oldItemContainer, resultContainer, amount);
                                        AddAmountToContainer(iUpg.attribute, "GENERIC_ATTACK_KNOCKBACK", CONSTANTS.GENERIC_ATTACK_KNOCKBACK, oldItemContainer, resultContainer, amount);

                                        if (iUpg.attribute.equalsIgnoreCase("GENERIC_ARMOR") ||
                                                iUpg.attribute.equalsIgnoreCase("GENERIC_ARMOR_TOUGHNESS") ||
                                                iUpg.attribute.equalsIgnoreCase("GENERIC_LUCK") ||
                                                iUpg.attribute.equalsIgnoreCase("GENERIC_MAX_HEALTH") ||
                                                iUpg.attribute.equalsIgnoreCase("GENERIC_KNOCKBACK_RESISTANCE") ||
                                                iUpg.attribute.equalsIgnoreCase("GENERIC_MOVEMENT_SPEED")
                                        )
                                            continue;

                                    }
                                    if (itemBefore.getType().toString().toLowerCase().contains("helmet") ||
                                            itemBefore.getType().toString().toLowerCase().contains("chestplate") ||
                                            itemBefore.getType().toString().toLowerCase().contains("leggings") ||
                                            itemBefore.getType().toString().toLowerCase().contains("boots") ||
                                            itemBefore.getType().equals(Material.SHIELD)
                                    ) {
                                        AddAmountToContainer(iUpg.attribute, "GENERIC_ARMOR", CONSTANTS.GENERIC_ARMOR, oldItemContainer, resultContainer, amount);
                                        AddAmountToContainer(iUpg.attribute, "GENERIC_ARMOR_TOUGHNESS", CONSTANTS.GENERIC_ARMOR_TOUGHNESS, oldItemContainer, resultContainer, amount);
                                        AddAmountToContainer(iUpg.attribute, "GENERIC_LUCK", CONSTANTS.GENERIC_LUCK, oldItemContainer, resultContainer, amount);
                                        AddAmountToContainer(iUpg.attribute, "GENERIC_MAX_HEALTH", CONSTANTS.GENERIC_MAX_HEALTH, oldItemContainer, resultContainer, amount);
                                        AddAmountToContainer(iUpg.attribute, "GENERIC_KNOCKBACK_RESISTANCE", CONSTANTS.GENERIC_KNOCKBACK_RESISTANCE, oldItemContainer, resultContainer, amount);
                                        AddAmountToContainer(iUpg.attribute, "GENERIC_MOVEMENT_SPEED", CONSTANTS.GENERIC_MOVEMENT_SPEED, oldItemContainer, resultContainer, amount);
                                        if (iUpg.attribute.equalsIgnoreCase("GENERIC_ATTACK_DAMAGE") ||
                                                iUpg.attribute.equalsIgnoreCase("GENERIC_ATTACK_SPEED") ||
                                                iUpg.attribute.equalsIgnoreCase("GENERIC_ATTACK_KNOCKBACK")
                                        )
                                            continue;
                                    }
                                    if (itemBefore.getType().equals(Material.SHIELD))
                                        iUpg.slot = EquipmentSlot.HAND.name();
                                    AttributeModifier attributeModifier = new AttributeModifier(UUID.randomUUID(), iUpg.attribute, amount, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.valueOf(iUpg.slot));
                                    resultMeta.addAttributeModifier(
                                            Attribute.valueOf(iUpg.attribute),
                                            attributeModifier
                                    );

                                    if (itemBefore.getType().equals(Material.SHIELD)) {
                                        iUpg.slot = EquipmentSlot.OFF_HAND.name();
                                        attributeModifier = new AttributeModifier(UUID.randomUUID(), iUpg.attribute, amount, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.valueOf(iUpg.slot));
                                        resultMeta.addAttributeModifier(
                                                Attribute.valueOf(iUpg.attribute),
                                                attributeModifier
                                        );
                                    }
                                    List<String> resultLore = resultMeta.getLore();
                                    if (resultLore == null)
                                        resultLore = new ArrayList<>();
                                    String attr = getStrAttr(iUpg.attribute);
                                    if (ingredient.getAmount() == 1)
                                        ReplaceLore(resultLore, plugin.CONSTANTS.HEXPattern(ChatColor.RED + attr + " +" + ChatColor.MAGIC + "-1"), attr);
                                    else
                                        ReplaceLore(resultLore, plugin.CONSTANTS.HEXPattern(ChatColor.RED + attr + " +" + ChatColor.MAGIC + "-1" + ChatColor.RED + "x" + ingredient.getAmount()), attr);

                                    resultMeta.setLore(resultLore);

                                }
                                result.setItemMeta(resultMeta);
                                e.setResult(result);
                                int finalUpgradeCount = upgradeCount;
                                e.getInventory().setRepairCost((int) ((ingredient.getAmount() / 2 + finalUpgradeCount) * plugin.mSettings.upgradeCostMultiplier));
                                plugin.getServer().getScheduler().runTask(plugin, () -> e.getInventory().setRepairCost((int) ((ingredient.getAmount() / 2 + finalUpgradeCount) * plugin.mSettings.upgradeCostMultiplier)));
                            }

                        }
                    }
                }
            }
        }

    }
}
