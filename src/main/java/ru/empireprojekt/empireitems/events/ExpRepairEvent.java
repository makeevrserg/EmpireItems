package ru.empireprojekt.empireitems.events;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import ru.empireprojekt.empireitems.EmpireConstants;
import ru.empireprojekt.empireitems.EmpireItems;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExpRepairEvent implements Listener {

    @EventHandler
    public void repairEvent(PlayerItemMendEvent e) {
        ChangeCustomDurability(e.getItem(), +e.getRepairAmount());
    }

    EmpireItems plugin;
    EmpireConstants CONSTANTS;
    public ExpRepairEvent(EmpireItems plugin){
        this.plugin = plugin;
        CONSTANTS = plugin.CONSTANTS;
    }


    @EventHandler
    public void anvilEvent(PrepareAnvilEvent e) {
        if (e.getResult() != null) {
            //Check on Experience
            ItemStack itemStack = e.getResult();
            ItemMeta resultMeta = itemStack.getItemMeta();
            if (resultMeta == null)
                return;
            PersistentDataContainer container = resultMeta.getPersistentDataContainer();
            int Damage;
            if (container.has(CONSTANTS.durabilityMechanicNamespace, PersistentDataType.INTEGER)) {
                @SuppressWarnings("ConstantConditions")
                int maxDurabilityCustom = container.get(CONSTANTS.maxCustomDurability, PersistentDataType.INTEGER);
                if (plugin.getServer().getPluginManager().getPlugin("Item-NBT-API") != null) {
                    NBTItem nbtItem = new NBTItem(itemStack);
                    Damage = nbtItem.getInteger("Damage");
                } else //noinspection deprecation
                    Damage = itemStack.getDurability();
                int maxDefault = itemStack.getType().getMaxDurability();
                int durability;
                durability = maxDurabilityCustom - Damage * maxDurabilityCustom / maxDefault;
                container = resultMeta.getPersistentDataContainer();
                container.set(CONSTANTS.durabilityMechanicNamespace, PersistentDataType.INTEGER, durability);
                itemStack.setItemMeta(resultMeta);
            }
        }
    }

    private void ChangeCustomDurability(ItemStack item, int damage) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        Integer durability = container.get(CONSTANTS.durabilityMechanicNamespace, PersistentDataType.INTEGER);
        if (durability == null)
            return;

        @SuppressWarnings("ConstantConditions")
        int maxDurabilityCustom = container.get(CONSTANTS.maxCustomDurability, PersistentDataType.INTEGER);
        durability += damage;
        if (durability <= 0) {
            item.setAmount(0);
            return;
        }
        if (durability > maxDurabilityCustom) {
            maxDurabilityCustom = durability;
            container.set(CONSTANTS.maxCustomDurability, PersistentDataType.INTEGER, maxDurabilityCustom);
        }

        container.set(CONSTANTS.durabilityMechanicNamespace, PersistentDataType.INTEGER, durability);
        item.setItemMeta(meta);
        if (plugin.getServer().getPluginManager().getPlugin("Item-NBT-API") != null) {
            NBTItem nbtItem = new NBTItem(item);
            int d = item.getType().getMaxDurability() - item.getType().getMaxDurability() * durability / maxDurabilityCustom;
            nbtItem.setInteger("Damage", d);
            nbtItem.applyNBT(item);
        }
    }


    //DONE
    @EventHandler
    public void durabilityEvent(PlayerItemDamageEvent e) {
        ChangeCustomDurability(e.getItem(), -e.getDamage());
    }

}
