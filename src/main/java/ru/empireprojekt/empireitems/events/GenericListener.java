package ru.empireprojekt.empireitems.events;

import org.bukkit.*;
import org.bukkit.block.data.type.Bed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.empireprojekt.empireitems.EmpireItems;

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
                System.out.println(ChatColor.GREEN + "Registerd event " + item.getItemMeta().getDisplayName());
                System.out.println(item_events.get(item.getItemMeta()));
                List<InteractEvent> events = item_events.get(item.getItemMeta());
                if (events != null)
                    for (InteractEvent ev : events)
                        if (ev.click.equalsIgnoreCase("RIGHT_CLICK")) {
                            if (ev.play_sound != null)
                                event.getPlayer().playSound(event.getPlayer().getLocation(),
                                        ev.play_sound, 1, 1
                                );
                            if (ev.play_particle != null)
                                event.getPlayer().spawnParticle(Particle.valueOf(ev.play_particle),
                                        event.getPlayer().getLocation().getX(), event.getPlayer().getLocation().getY() + 2, event.getPlayer().getLocation().getZ(),
                                        ev.particle_count, 0, 0, 0, ev.particle_time);
                            if (ev.execute_commands!=null){
                                for (String cmd:ev.execute_commands)
                                    if (!ev.as_console)
                                        event.getPlayer().performCommand(cmd);
                                    else
                                        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(),cmd);
                            }
                        }

            }
    }
}
