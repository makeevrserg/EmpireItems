package ru.empireprojekt.empireitems.enchants;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ru.empireprojekt.empireitems.EmpireConstants;
import ru.empireprojekt.empireitems.EmpireItems;

public class LavaWalker implements Listener {

    EmpireItems plugin;
    EmpireConstants CONSTANTS;
    public LavaWalker(EmpireItems plugin){
        this.plugin = plugin;
        this.CONSTANTS = plugin.CONSTANTS;
    }

    private void createBlocks(Block block){
        block.setType(Material.COBBLESTONE);
        block.getRelative(BlockFace.EAST).setType(Material.COBBLESTONE);
        block.getRelative(BlockFace.WEST).setType(Material.COBBLESTONE);
        block.getRelative(BlockFace.SOUTH).setType(Material.COBBLESTONE);
        block.getRelative(BlockFace.SOUTH_EAST).setType(Material.COBBLESTONE);
        block.getRelative(BlockFace.SOUTH_WEST).setType(Material.COBBLESTONE);
        block.getRelative(BlockFace.NORTH).setType(Material.COBBLESTONE);
        block.getRelative(BlockFace.NORTH_EAST).setType(Material.COBBLESTONE);
        block.getRelative(BlockFace.NORTH_WEST).setType(Material.COBBLESTONE);
    }
    @EventHandler
    private void playerMoveEvent(PlayerMoveEvent e) {
        ItemStack itemStack = e.getPlayer().getInventory().getBoots();
        if (itemStack==null)
            return;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta==null)
            return;
        if (!itemMeta.getPersistentDataContainer().has(CONSTANTS.lavaWalker, PersistentDataType.INTEGER))
            return;
        Block onToBlock = e.getTo().getBlock().getRelative(BlockFace.DOWN);
        if (onToBlock.getType() == Material.LAVA) {
            createBlocks(onToBlock);
        }
    }
}
