package ru.empireprojekt.empireitems.events;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import ru.empireprojekt.empireitems.EmpireItems;

import java.util.ArrayList;

public class MobDropEvent implements Listener {

    private ArrayList<Location> blockLocations;
    EmpireItems plugin;

    public MobDropEvent(EmpireItems plugin){
        this.plugin = plugin;
        blockLocations = new ArrayList<>();
    }



    @EventHandler
    public void blockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        //Чекаем есть ли дроп с блока в конфигах
        for (String blockKey : plugin.blockDrops.keySet())
            if (Material.getMaterial(blockKey) != null && Material.getMaterial(blockKey) == e.getBlock().getBlockData().getMaterial()) {
                if (blockLocations.size() > 8)
                    blockLocations.remove(0);
                if (blockLocations.contains(block.getLocation()))
                    return;

                for (Drop drop : plugin.blockDrops.get(blockKey))
                    e.setDropItems(!DropManager(drop, block));

                blockLocations.add(block.getLocation());
            }


    }

    private boolean DropManager(Drop drop, Object obj) {
        //Чекаем на правильность предмета
        ItemStack item = plugin.items.get(drop.item);

        if (item == null) {
            System.out.println(ChatColor.AQUA + "[EmpireItems]" + "Введен неверный матреиал " + drop.item);
            return false;
        }

        int amount = drop.min_amount + (int) (Math.random() * drop.max_amount);
        double chance = (Math.random() * 100);
        boolean isDrop = false;
        if (drop.chance > chance)
            for (int i = 0; i < amount; ++i)
                if (obj instanceof Entity) {
                    ((Entity) obj).getLocation().getWorld().dropItem(((Entity) obj).getLocation(), item);
                    isDrop = true;
                } else if (obj instanceof Block) {
                    ((Block) obj).getLocation().getWorld().dropItem(((Block) obj).getLocation(), item);
                    isDrop = true;
                }

        return isDrop;
    }

    @EventHandler
    public void mobDeath(EntityDeathEvent e) {
        Entity entity = e.getEntity();
        for (String sEnt : plugin.mobDrops.keySet())
            if (EntityType.valueOf(sEnt) == entity.getType())
                for (Drop drop : plugin.mobDrops.get(sEnt))
                    DropManager(drop, entity);
    }

}
