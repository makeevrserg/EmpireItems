package ru.empireprojekt.empireitems.enchants;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ru.empireprojekt.empireitems.EmpireItems;

public class Grenade implements Listener {
    EmpireItems plugin;

    public Grenade(EmpireItems plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Player))
            return;
        Player player = (Player) (e.getEntity().getShooter());
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        ItemMeta meta = itemStack.getItemMeta();
        NamespacedKey itemExplosionNamespace = new NamespacedKey(plugin, "onHitGroundExplosion");
        if (meta == null)
            return;
        System.out.println("Check" + meta.getPersistentDataContainer().has(itemExplosionNamespace, PersistentDataType.INTEGER));
        if (!meta.getPersistentDataContainer().has(itemExplosionNamespace, PersistentDataType.INTEGER))
            return;
        Integer explosionPower = meta.getPersistentDataContainer().get(itemExplosionNamespace, PersistentDataType.INTEGER);
        if (explosionPower != null) {
            e.getEntity().getWorld().createExplosion(e.getEntity().getLocation(), explosionPower);
        }


    }


}
