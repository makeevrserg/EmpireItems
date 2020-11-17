package ru.empireprojekt.empireitems.events;

import org.bukkit.*;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.empireprojekt.empireitems.EmpireItems;
import ru.empireprojekt.empireitems.ItemManager.mPotionEffect;

import java.util.List;
import java.util.Map;

public class GenericListener implements Listener {
    Map<ItemMeta, List<InteractEvent>> item_events;

    public GenericListener(Map<ItemMeta, List<InteractEvent>> item_events) {
        this.item_events = item_events;
    }

    public void ReloadListener(Map<ItemMeta, List<InteractEvent>> item_events) {
        this.item_events = item_events;
        System.out.println(ChatColor.GREEN + "Listener reloaded!");
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR)
            if (event.getItem() != null) {
                ItemStack item = event.getItem();
                List<InteractEvent> events = item_events.get(item.getItemMeta());//Получаем эвент конкретного предмета
                if (events != null)
                    for (InteractEvent ev : events) {
                        if (!ev.click.equalsIgnoreCase("RIGHT_CLICK"))//Если нет эвента - скипаем
                            continue;
                        HandleEvent(ev, event.getPlayer());
                    }

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
        if (ev.potion_effects!=null)
            player.addPotionEffects(ev.potion_effects);
        if (ev.remove_potion_effect!=null)
            for (PotionEffectType effect:ev.remove_potion_effect)
                player.removePotionEffect(effect);
    }




    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event){
        ItemStack item = event.getItem();
        List<InteractEvent> events = item_events.get(item.getItemMeta());//Получаем эвент конкретного предмета
        Player player = event.getPlayer();
        if (events != null) {
            for (InteractEvent ev : events) {
                if (ev.click.equalsIgnoreCase("drink"))
                    HandleEvent(ev,player);
                if (ev.click.equalsIgnoreCase("eat"))
                    HandleEvent(ev,player);
            }
        }
    }
}
