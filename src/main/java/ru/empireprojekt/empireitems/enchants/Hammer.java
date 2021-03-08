package ru.empireprojekt.empireitems.enchants;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ru.empireprojekt.empireitems.EmpireConstants;
import ru.empireprojekt.empireitems.EmpireItems;

import java.util.*;

public class Hammer implements Listener {

    private Map<Player, Integer> blockFace;
    EmpireItems plugin;
    EmpireConstants CONSTANTS;

    public Hammer(EmpireItems plugin) {

        this.plugin = plugin;
        this.CONSTANTS = plugin.CONSTANTS;
        blockFace = new HashMap<>();
    }

    @EventHandler
    void PlayerInteractEvent(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack itemInHand = p.getInventory().getItemInMainHand();
        BlockFace bFace = e.getBlockFace();
        int side;
        String strside = bFace.name();
        if ((strside.equalsIgnoreCase("up")) || (strside.equalsIgnoreCase("down")))
            side = 0;
        else if (strside.equalsIgnoreCase("south") || strside.equalsIgnoreCase("north"))
            side = 1;
        else side = 2;
        blockFace.put(p, side);
    }

    @EventHandler
    void PlayerBreakEvent(BlockBreakEvent e) {
        if (e.isCancelled())
            return;
        ItemStack itemStack = e.getPlayer().getInventory().getItemInMainHand();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta==null)
            return;
        if (!itemMeta.getPersistentDataContainer().has(CONSTANTS.itemHammer, PersistentDataType.INTEGER))
            return;
        Integer side = blockFace.get(e.getPlayer());
        blockFace.remove(e.getPlayer());
        if (side != null) {
            List<Block> blocks = new ArrayList<>();
            Block b = e.getBlock();
            switch (side) {
                case 0:
                    blocks.add(b.getRelative(BlockFace.NORTH));
                    blocks.add(b.getRelative(BlockFace.SOUTH));
                    blocks.add(b.getRelative(BlockFace.WEST));
                    blocks.add(b.getRelative(BlockFace.EAST));
                    blocks.add(b.getRelative(BlockFace.NORTH_WEST));
                    blocks.add(b.getRelative(BlockFace.NORTH_EAST));
                    blocks.add(b.getRelative(BlockFace.SOUTH_EAST));
                    blocks.add(b.getRelative(BlockFace.SOUTH_WEST));
                    break;
                case 1:
                    blocks.add(b.getRelative(BlockFace.WEST));
                    blocks.add(b.getRelative(BlockFace.EAST));
                    blocks.add(b.getRelative(BlockFace.UP));
                    blocks.add(b.getRelative(BlockFace.DOWN));

                    blocks.add(b.getRelative(BlockFace.UP).getRelative(BlockFace.WEST));
                    blocks.add(b.getRelative(BlockFace.UP).getRelative(BlockFace.EAST));
                    blocks.add(b.getRelative(BlockFace.DOWN).getRelative(BlockFace.WEST));
                    blocks.add(b.getRelative(BlockFace.DOWN).getRelative(BlockFace.EAST));
                    break;
                default:
                    blocks.add(b.getRelative(BlockFace.SOUTH));
                    blocks.add(b.getRelative(BlockFace.NORTH));
                    blocks.add(b.getRelative(BlockFace.UP));
                    blocks.add(b.getRelative(BlockFace.DOWN));

                    blocks.add(b.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH));
                    blocks.add(b.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH));
                    blocks.add(b.getRelative(BlockFace.DOWN).getRelative(BlockFace.SOUTH));
                    blocks.add(b.getRelative(BlockFace.DOWN).getRelative(BlockFace.NORTH));
                    break;
            }
            for (Block block : blocks) {
                String blockName = block.getType().toString().toLowerCase();
                //shovel
                //pickaxe
                List<String> keys = Arrays.asList("stone",
                        "granite",
                        "diorite",
                        "andesite",
                        "ore",
                        "block",
                        "cobblestone",
                        "sandstone",
                        "purpur",
                        "prismarine",
                        "brick",
                        "bars",
                        "chain",
                        "terracotta",
                        "quartz",
                        "ice",
                        "magma",
                        "concrete",
                        "blackstone",
                        "netherrack",
                        "basalt");


                for (String key:keys)
                    if (blockName.contains(key)){

                        block.breakNaturally();
                        break;
                    }



                //axe

                //block.breakNaturally();
            }
        }
    }
}
