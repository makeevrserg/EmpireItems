package ru.empireprojekt.empireitems.events;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import ru.empireprojekt.empireitems.EmpireItems;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericListener implements Listener {
    Map<ItemMeta, List<InteractEvent>> item_events;
    EmpireItems plugin;

    public GenericListener(EmpireItems plugin, Map<ItemMeta, List<InteractEvent>> item_events) {
        this.plugin = plugin;
        this.item_events = item_events;
    }

    public void ReloadListener(Map<ItemMeta, List<InteractEvent>> item_events) {
        this.item_events = item_events;
        System.out.println(ChatColor.GREEN + "Listener reloaded!");
    }


    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        Projectile projectile = e.getEntity();
        if (projectile instanceof Snowball) {
            ItemStack itemStack = ((Snowball) projectile).getItem();
            ItemMeta meta = itemStack.getItemMeta();
            NamespacedKey itemExplosionNamespace = new NamespacedKey(plugin, "onHitGroundExplosion");
            if (meta.getPersistentDataContainer().has(itemExplosionNamespace, PersistentDataType.INTEGER)) {
                int explosionPower = meta.getPersistentDataContainer().get(itemExplosionNamespace, PersistentDataType.INTEGER);
                projectile.getWorld().createExplosion(projectile.getLocation(), explosionPower);
            }

        }
    }


    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR)
            if (event.getItem() != null) {
                ItemStack item = event.getItem();
                List<InteractEvent> events = item_events.get(item.getItemMeta());//Получаем эвент конкретного предмета
                if (events != null)
                    for (InteractEvent ev : events)
                        if (ev.click.equalsIgnoreCase("RIGHT_CLICK"))
                            HandleEvent(ev, event.getPlayer());


            }

    }


    private void HandleEvent(InteractEvent ev, Player player) {
        if (ev.play_sound != null)
            player.playSound(player.getLocation(),
                    ev.play_sound, 1, 1
            );
        if (ev.play_particle != null)
            player.spawnParticle(Particle.valueOf(ev.play_particle),
                    player.getLocation().getX(), player.getLocation().getY() + 2, player.getLocation().getZ(),
                    ev.particle_count, 0, 0, 0, ev.particle_time);
        if (ev.execute_commands != null) {
            for (String cmd : ev.execute_commands)
                if (!ev.as_console)
                    player.performCommand(cmd);
                else
                    Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
        }
        if (ev.potion_effects != null)
            player.addPotionEffects(ev.potion_effects);
        if (ev.remove_potion_effect != null)
            for (PotionEffectType effect : ev.remove_potion_effect)
                player.removePotionEffect(effect);
    }


    //DONE
    @EventHandler
    public void durabilityEvent(PlayerItemDamageEvent e) {
        ItemStack item = e.getItem();
        ItemMeta meta = item.getItemMeta();
        NamespacedKey durabilityMechanicNamespace = new NamespacedKey(plugin, "durability");
        NamespacedKey maxCustomDurability = new NamespacedKey(plugin, "maxCustomDurability");
        if (meta == null)
            return;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(durabilityMechanicNamespace, PersistentDataType.INTEGER))
            return;

        int durability = container.get(durabilityMechanicNamespace, PersistentDataType.INTEGER);
        durability -= e.getDamage();
        if (durability <= 0) {
            item.setAmount(0);
            return;
        }
        container.set(durabilityMechanicNamespace, PersistentDataType.INTEGER, durability);
        if (meta.getLore() == null || meta.getLore().size() == 0) {
            List<String> list = new ArrayList<String>();
            list.add("&7Прочность: " + durability);
            plugin.HEXPattern(list);
            meta.setLore(list);
        } else {
            List<String> lore = meta.getLore();
            if (!lore.contains("Прочность"))
                lore.add(plugin.HEXPattern("&7Прочность: " + durability));
            else
                for (int i = 0; i < lore.size(); ++i)
                    if (lore.get(i).contains("Прочность"))
                        lore.set(i, plugin.HEXPattern("&7Прочность: " + durability));

            meta.setLore(lore);

        }
        item.setItemMeta(meta);
        NBTItem nbtItem = new NBTItem(item);
        int d = item.getType().getMaxDurability() - (int) (item.getType().getMaxDurability() / (
                meta.getPersistentDataContainer().get(maxCustomDurability, PersistentDataType.INTEGER)
                        / (double) durability));
        nbtItem.setInteger("Damage", d);
        nbtItem.applyNBT(item);
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        //Чекаем есть ли дроп с блока в конфигах
        for (String blockKey : plugin.blockDrops.keySet())
            if (Material.getMaterial(blockKey) != null && Material.getMaterial(blockKey) == e.getBlock().getBlockData().getMaterial())
                for (Drop drop : plugin.blockDrops.get(blockKey))
                    DropManager(drop, block);


    }

    private void DropManager(Drop drop, Object obj) {
        ItemStack item;
        //Чекаем на правильность предмета
        if (plugin.items.containsKey(drop.item))
            item = plugin.items.get(drop.item);
        else if (Material.getMaterial(drop.item) != null)
            item = new ItemStack(Material.getMaterial(drop.item));
        else {
            System.out.println("Введен неверный матреиал " + drop.item);
            return;
        }

        int amount = drop.min_amount + (int) (Math.random() * drop.max_amount);
        int chance = (int) (Math.random() * 100);
        if (drop.chance > chance)
            for (int i = 0; i < amount; ++i)
                if (obj instanceof Entity)
                    ((Entity) obj).getLocation().getWorld().dropItem(((Entity) obj).getLocation(), item);
                else if (obj instanceof Block)
                    ((Block) obj).getLocation().getWorld().dropItem(((Block) obj).getLocation(), item);
    }

    @EventHandler
    public void mobDeath(EntityDeathEvent e) {
        Entity entity = e.getEntity();
        ItemStack item;
        for (String sEnt : plugin.mobDrops.keySet())
            if (EntityType.valueOf(sEnt) == entity.getType())
                for (Drop drop : plugin.mobDrops.get(sEnt))
                    DropManager(drop, entity);
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        List<InteractEvent> events = item_events.get(item.getItemMeta());//Получаем эвент конкретного предмета
        Player player = event.getPlayer();
        if (events != null)
            for (InteractEvent ev : events)
                if (ev.click.equalsIgnoreCase("drink"))
                    HandleEvent(ev, player);
                else if (ev.click.equalsIgnoreCase("eat"))
                    HandleEvent(ev, player);


    }
}
