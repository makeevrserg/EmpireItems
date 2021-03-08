package ru.empireprojekt.empireitems.events;

import de.tr7zw.nbtapi.NBTItem;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import ru.empireprojekt.empireitems.EmpireConstants;
import ru.empireprojekt.empireitems.EmpireItems;
import ru.empireprojekt.empireitems.enchants.Grenade;
import ru.empireprojekt.empireitems.enchants.Hammer;
import ru.empireprojekt.empireitems.enchants.LavaWalker;
import ru.empireprojekt.empireitems.enchants.Vampirism;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GenericListener implements Listener {
    private Map<String, InteractEvent[]> item_events;
    private EmpireItems plugin;
    private EmpireConstants CONSTANTS;

    private List<String> messages = new ArrayList<>();
    private DiscordListener discordListener;
    private Hammer hammerListener;
    private LavaWalker lavaWalker;
    private Vampirism vampirism;
    private Grenade grenade;
    private MobDropEvent mobDropEvent;
    private ExpRepairEvent expRepairEvent;
    private ItemUpgradeEvent itemUpgradeEvent;

    public GenericListener(EmpireItems plugin, Map<String, InteractEvent[]> item_events) {
        this.plugin = plugin;
        this.CONSTANTS = plugin.CONSTANTS;
        this.item_events = item_events;
        if (plugin.getServer().getPluginManager().getPlugin("DiscordSRV") != null)
            discordListener = new DiscordListener(plugin);
        else
            System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.YELLOW + "DiscordSRV Is not installed!");

        //events.enchants
        hammerListener = new Hammer(plugin);
        lavaWalker = new LavaWalker(plugin);
        vampirism = new Vampirism(plugin);
        grenade = new Grenade(plugin);
        mobDropEvent = new MobDropEvent(plugin);
        expRepairEvent = new ExpRepairEvent(plugin);
        itemUpgradeEvent = new ItemUpgradeEvent(plugin);
        plugin.getServer().getPluginManager().registerEvents(hammerListener, plugin);
        plugin.getServer().getPluginManager().registerEvents(lavaWalker, plugin);
        plugin.getServer().getPluginManager().registerEvents(vampirism, plugin);
        plugin.getServer().getPluginManager().registerEvents(grenade, plugin);
        plugin.getServer().getPluginManager().registerEvents(mobDropEvent, plugin);
        plugin.getServer().getPluginManager().registerEvents(expRepairEvent, plugin);
        plugin.getServer().getPluginManager().registerEvents(itemUpgradeEvent, plugin);
    }

    public void UnregisterListener() {
        ProjectileHitEvent.getHandlerList().unregister(this);
        PlayerInteractEvent.getHandlerList().unregister(this);
        PlayerItemDamageEvent.getHandlerList().unregister(this);
        BlockBreakEvent.getHandlerList().unregister(this);
        EntityDeathEvent.getHandlerList().unregister(this);
        PlayerItemConsumeEvent.getHandlerList().unregister(this);
        AsyncPlayerChatEvent.getHandlerList().unregister(this);
        PrepareAnvilEvent.getHandlerList().unregister(this);
        InventoryClickEvent.getHandlerList().unregister(this);
        if (hammerListener != null) {
            PlayerInteractEvent.getHandlerList().unregister(hammerListener);
            BlockBreakEvent.getHandlerList().unregister(hammerListener);
        }
        if (lavaWalker != null) {
            PlayerMoveEvent.getHandlerList().unregister(lavaWalker);
        }
        if (vampirism != null) {
            EntityDamageByEntityEvent.getHandlerList().unregister(vampirism);
        }
        if (grenade != null) {
            ProjectileHitEvent.getHandlerList().unregister(grenade);
        }
        if (mobDropEvent != null) {
            BlockBreakEvent.getHandlerList().unregister(mobDropEvent);
            EntityDeathEvent.getHandlerList().unregister(mobDropEvent);
        }
        if (expRepairEvent != null) {
            PlayerItemMendEvent.getHandlerList().unregister(expRepairEvent);
            PrepareAnvilEvent.getHandlerList().unregister(expRepairEvent);
            PlayerItemDamageEvent.getHandlerList().unregister(expRepairEvent);
        }
        if (itemUpgradeEvent != null) {
            InventoryClickEvent.getHandlerList().unregister(itemUpgradeEvent);
            PrepareAnvilEvent.getHandlerList().unregister(itemUpgradeEvent);
        }
        if (discordListener != null)
            discordListener.onDisable();
        messages = new ArrayList<>();

    }


    List<String> getMessages() {
        return messages;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        String msg = event.getMessage();
        String newMessage = "" + msg + " ";
        if (discordListener != null)
            messages.add(newMessage);
        event.setMessage(plugin.CONSTANTS.GetEmoji(msg, plugin.CONSTANTS.getEmojiPattern()));
    }


    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player player = (Player) e.getDamager();
            ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();
            if (meta == null)
                return;

            if (meta.getPersistentDataContainer().isEmpty())
                return;
            if (!meta.getPersistentDataContainer().has(CONSTANTS.empireID,PersistentDataType.STRING))
                return;

            String id = meta.getPersistentDataContainer().get(CONSTANTS.empireID, PersistentDataType.STRING);
            

            InteractEvent[] events = item_events.get(id);//Получаем эвент конкретного предмета
            if (events != null)
                for (InteractEvent ev : events)
                    if (ev.click.equalsIgnoreCase("entity_damage")) HandleEvent(ev, player);
        }

    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        ItemStack itemStack = p.getInventory().getItemInMainHand();
        ManageEvent(itemStack, event.getAction().name(), p);
        ManageEvent(itemStack, event.getAction().name(), p);
    }

    private void ManageEvent(ItemStack item, String eventName, Player p) {
        if (item.getItemMeta() != null) {
            String id = item.getItemMeta().getPersistentDataContainer().get(CONSTANTS.empireID, PersistentDataType.STRING);
            InteractEvent[] events = item_events.get(id);//Получаем эвент конкретного предмета
            if (events != null)
                for (InteractEvent ev : events)
                    if (ev.click.equalsIgnoreCase(eventName))
                        HandleEvent(ev, p);
        }
    }

    private void ManageDurability(ItemStack itemStack, int takeDurability) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            Integer durability = container.get(CONSTANTS.durabilityMechanicNamespace, PersistentDataType.INTEGER);
            if (durability != null) {
                durability += takeDurability;
                container.set(CONSTANTS.durabilityMechanicNamespace, PersistentDataType.INTEGER, durability);
                List<String> itemLore = meta.getLore();
                if (itemLore == null || itemLore.size() == 0)
                    itemLore = new ArrayList<>();
                itemLore.add(plugin.CONSTANTS.HEXPattern("&7Использований: " + durability));
                for (int i = 0; i < itemLore.size() - 1; ++i)
                    if (itemLore.get(i).contains("Использований")) {
                        itemLore.remove(i);
                        break;
                    }
                meta.setLore(itemLore);
                itemStack.setItemMeta(meta);
                if (durability <= 0)
                    itemStack.setAmount(0);
            }
        }
    }


    private void HandleEvent(InteractEvent ev, Player player) {
        if (ev.takeDurability != 0) {
            ItemStack mainHandItem = player.getInventory().getItemInMainHand();
            ManageDurability(mainHandItem, ev.takeDurability);
            if (mainHandItem.getItemMeta() == null || mainHandItem.getItemMeta().getPersistentDataContainer().get(CONSTANTS.durabilityMechanicNamespace, PersistentDataType.INTEGER) == null)
                ManageDurability(player.getInventory().getItemInOffHand(), ev.takeDurability);

        }
        if (ev.play_sound != null)
            player.getWorld().playSound(player.getLocation(),
                    ev.play_sound, 1, 1);
        if (ev.play_particle != null)
            player.getWorld().spawnParticle(Particle.valueOf(ev.play_particle),
                    player.getLocation().getX(), player.getLocation().getY() + 2, player.getLocation().getZ(),
                    ev.particle_count, 0, 0, 0, ev.particle_time);
        if (ev.execute_commands != null && plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            for (String cmd : ev.execute_commands)
                if (!ev.as_console)
                    player.performCommand(PlaceholderAPI.setPlaceholders(player, cmd));
                else
                    Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), PlaceholderAPI.setPlaceholders(player, cmd));
        }
        if (ev.potion_effects != null)
            player.addPotionEffects(ev.potion_effects);
        if (ev.remove_potion_effect != null)
            for (PotionEffectType effect : ev.remove_potion_effect)
                player.removePotionEffect(effect);
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemMeta meta = event.getItem().getItemMeta();
        if (meta == null)
            return;
        String id = meta.getPersistentDataContainer().get(CONSTANTS.empireID, PersistentDataType.STRING);
        InteractEvent[] events = item_events.get(id);//Получаем эвент конкретного предмета
        Player player = event.getPlayer();
        if (events != null)
            for (InteractEvent ev : events)
                if (ev.click.equalsIgnoreCase("drink"))
                    HandleEvent(ev, player);
                else if (ev.click.equalsIgnoreCase("eat"))
                    HandleEvent(ev, player);


    }
}
