package ru.empireprojekt.empireitems.enchants;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ru.empireprojekt.empireitems.EmpireConstants;
import ru.empireprojekt.empireitems.EmpireItems;

public class Vampirism implements Listener {

    EmpireItems plugin;
    EmpireConstants CONSTANTS;
    EmpireItems.PluginSettings mSettings;

    public Vampirism(EmpireItems plugin) {
        this.plugin = plugin;
        CONSTANTS = plugin.CONSTANTS;
        mSettings = plugin.mSettings;
    }

    @EventHandler
    private void onEntityDamate(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player))
            return;
        Player p = (Player) e.getDamager();
        ItemStack itemStack = p.getInventory().getItemInMainHand();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return;

        Integer vampSize = itemMeta.getPersistentDataContainer().get(CONSTANTS.vampirism, PersistentDataType.INTEGER);
        if (vampSize == null)
            return;
        double damage = e.getFinalDamage();
        double playerHealth = p.getHealth();
        double playerMaxHealth = p.getMaxHealth();
        double toAddHealth = damage*mSettings.vampirismMultiplier+vampSize;
        System.out.println("Max="+playerMaxHealth);
        System.out.println("toAdd="+toAddHealth);
        p.setHealth(Math.min(toAddHealth + playerHealth, playerMaxHealth));

    }
}
