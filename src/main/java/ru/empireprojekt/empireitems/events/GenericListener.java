package ru.empireprojekt.empireitems.events;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.empireprojekt.empireitems.EmpireItems;

import java.util.Map;

public class GenericListener implements Listener {
    Map<ItemMeta,InteractEvent> item_events;
    public GenericListener(Map<ItemMeta,InteractEvent> item_events){
        this.item_events=item_events;
    }
    @EventHandler
    public void onRightClick(PlayerInteractEvent event){
        if (event.getAction() == Action.RIGHT_CLICK_AIR)
            if (event.getItem() != null){
                ItemStack item= event.getItem();
                System.out.println(ChatColor.GREEN+"Registerd event "+item.getItemMeta().getDisplayName());
                System.out.println(item_events.get(item.getItemMeta()));
                event.getPlayer().playSound(event.getPlayer().getLocation(),item_events.get(item.getItemMeta()).play_sound,1,1);
            }
    }
}
