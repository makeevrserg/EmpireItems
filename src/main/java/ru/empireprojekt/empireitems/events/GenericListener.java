package ru.empireprojekt.empireitems.events;

import com.google.gson.internal.$Gson$Preconditions;
import de.tr7zw.nbtapi.NBTItem;
import github.scarsz.discordsrv.hooks.PlaceholderAPIExpansion;
import github.scarsz.discordsrv.util.PlaceholderUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import ru.empireprojekt.empireitems.EmpireItems;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.*;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.util.DiscordUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenericListener implements Listener {
    Map<String, List<InteractEvent>> item_events;
    EmpireItems plugin;

    public GenericListener(EmpireItems plugin, Map<String, List<InteractEvent>> item_events) {
        this.plugin = plugin;
        this.item_events = item_events;
        if (plugin.getServer().getPluginManager().getPlugin("discordsrv") != null)
            DiscordSRV.api.subscribe(this);
        else
            System.out.println(ChatColor.YELLOW + "DiscordSRV Is not installed!");
    }

    public void UnregisterListener() {
        ProjectileHitEvent.getHandlerList().unregister(this);
        PlayerInteractEvent.getHandlerList().unregister(this);
        PlayerItemDamageEvent.getHandlerList().unregister(this);
        BlockBreakEvent.getHandlerList().unregister(this);
        EntityDeathEvent.getHandlerList().unregister(this);
        PlayerItemConsumeEvent.getHandlerList().unregister(this);
        AsyncPlayerChatEvent.getHandlerList().unregister(this);
        messages = new ArrayList<>();

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


    private final Pattern emojiPattern = Pattern.compile(":([a-zA-Z0-9_]{3}|[a-zA-Z0-9_]{4}|[a-zA-Z0-9_]{5}|[a-zA-Z0-9_]{6}|[a-zA-Z0-9_]{7}|[a-zA-Z0-9_]{8}|[a-zA-Z0-9_]{9}|[a-zA-Z0-9_]{10}|[a-zA-Z0-9_]{11}|[a-zA-Z0-9_]{12}|[a-zA-Z0-9_]{13} ):");

    List<String> messages = new ArrayList<String>();

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        String msg = event.getMessage();
        String newMessage = "" + msg + " ";
        messages.add(newMessage);
        event.setMessage(GetEmoji(msg, emojiPattern));
    }

    private String GetEmoji(String msg, Pattern pattern) {
        Matcher matcher = pattern.matcher(msg);
        while (matcher.find()) {
            String emoji = msg.substring(matcher.start(), matcher.end());
            String toReplace = plugin.emojis.get(emoji);
            if (toReplace == null)
                toReplace = msg.replaceAll(":", ".");

            msg = msg.replace(emoji, toReplace + "");


            matcher = pattern.matcher(msg);
        }
        return msg;
    }

    @Subscribe(priority = ListenerPriority.MONITOR)
    public void onChatMessageFromDiscord(DiscordGuildMessagePostProcessEvent event) { // From Discord to in-game
        // TODO: Add permission checking for Discord
        String message = event.getProcessedMessage();
        event.setProcessedMessage(GetEmoji(message, emojiPattern));
    }

    @Subscribe(priority = ListenerPriority.HIGHEST)
    public void onChatMessageFromInGame(GameChatMessagePreProcessEvent event) { // From in-game to Discord
        String message = event.getMessage();

        int count = messages.size();
        for (int i = 0; i < count; i++) {
            event.setMessage(messages.get(0).toString());
            messages.remove(0);
        }

        //event.setMessage(message);
    }

//    @EventHandler
//    public void onInventoryClosedEvent(InventoryCloseEvent e) {
//        if (openedChests.contains(e.getPlayer().getUniqueId().toString()))
//            openedChests.remove(e.getPlayer().getUniqueId().toString());
//    }
//
//    List<String> openedChests = new ArrayList<String>();
//
//    @EventHandler
//    public void onInventoryOpenEvent(InventoryOpenEvent e) {
//        if (e.getInventory().getType() == InventoryType.CHEST && !openedChests.contains(e.getPlayer().getUniqueId().toString())) {
//            openedChests.add(e.getPlayer().getUniqueId().toString());
//            e.setCancelled(true);
//            System.out.println("Inventory");
//            Inventory inv = e.getInventory();
//            Inventory inventory = Bukkit.createInventory(null, inv.getSize(), "\ue11e");
//            ItemStack items[] = inv.getContents();
//            e.getPlayer().closeInventory();
//            e.getPlayer().openInventory(inventory);
//        }
//    }


    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player player = (Player) e.getDamager();
            ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();
            if (meta != null) {
                NamespacedKey empireID = new NamespacedKey(plugin, "id");
                String id = meta.getPersistentDataContainer().get(empireID, PersistentDataType.STRING);
                List<InteractEvent> events = item_events.get(id);//Получаем эвент конкретного предмета
                if (events != null)
                    for (InteractEvent ev : events)
                        if (ev.click.equalsIgnoreCase("entity_damage"))
                            HandleEvent(ev, player);
            }

        }
    }


    public void PlaySplash(Player p, String splash) {
        if (!plugin.items.containsKey(splash) || p == null) {
            System.out.println("Splash отсутствует либо игрок=null!: " + splash);
            return;
        }
        ItemStack itemInHand = p.getInventory().getItemInMainHand().clone();
        ItemStack totemItem = plugin.items.get(splash).clone();
        p.getInventory().setItemInMainHand(totemItem);
        p.playEffect(EntityEffect.TOTEM_RESURRECT);
        p.getInventory().setItemInMainHand(itemInHand);
    }


    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        ItemStack itemStack = p.getInventory().getItemInMainHand();
        ItemMeta meta = itemStack.getItemMeta();

        ManageEvent(p.getInventory().getItemInMainHand(),event.getAction().name(),p);
        ManageEvent(p.getInventory().getItemInOffHand(),event.getAction().name(),p);
    }

    private void ManageEvent(ItemStack item, String eventName, Player p) {
        if (item.getItemMeta() != null) {
            NamespacedKey empireID = new NamespacedKey(plugin, "id");
            String id = item.getItemMeta().getPersistentDataContainer().get(empireID, PersistentDataType.STRING);
            List<InteractEvent> events = item_events.get(id);//Получаем эвент конкретного предмета
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
            NamespacedKey durabilityMechanicNamespace = new NamespacedKey(plugin, "durability");
            if (container.has(durabilityMechanicNamespace, PersistentDataType.INTEGER)) {
                int durability = container.get(durabilityMechanicNamespace, PersistentDataType.INTEGER);
                durability += takeDurability;
                container.set(durabilityMechanicNamespace, PersistentDataType.INTEGER, durability);
                List<String> itemLore = meta.getLore();
                if (itemLore == null || itemLore.size() == 0)
                    itemLore = new ArrayList<>();
                itemLore.add(plugin.HEXPattern("&7Использований: " + durability));
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
            NamespacedKey durabilityMechanicNamespace = new NamespacedKey(plugin, "durability");
            if (mainHandItem.getItemMeta() == null || mainHandItem.getItemMeta().getPersistentDataContainer().get(durabilityMechanicNamespace, PersistentDataType.INTEGER) == null)
                ManageDurability(player.getInventory().getItemInOffHand(), ev.takeDurability);

        }
        if (ev.play_sound != null)
            player.getWorld().playSound(player.getLocation(),
                    ev.play_sound, 1, 1);
        if (ev.play_particle != null)
            player.getWorld().spawnParticle(Particle.valueOf(ev.play_particle),
                    player.getLocation().getX(), player.getLocation().getY() + 2, player.getLocation().getZ(),
                    ev.particle_count, 0, 0, 0, ev.particle_time);
        if (ev.execute_commands != null) {
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
    public void repairEvent(PlayerItemMendEvent e) {
        ChangeCustomDurability(e.getItem(), +e.getRepairAmount());
    }

    @EventHandler
    public void anvilEvent(PrepareAnvilEvent e) {
        if (e.getResult() != null) {
            ItemStack itemStack = e.getResult();
            ItemMeta meta = itemStack.getItemMeta();
            if (meta == null)
                return;
            NamespacedKey durabilityMechanicNamespace = new NamespacedKey(plugin, "durability");
            NamespacedKey maxCustomDurability = new NamespacedKey(plugin, "maxCustomDurability");
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (!container.has(durabilityMechanicNamespace, PersistentDataType.INTEGER))
                return;

            int maxDurabilityCustom = container.get(maxCustomDurability, PersistentDataType.INTEGER);
            NBTItem nbtItem = new NBTItem(itemStack);
            int Damage = nbtItem.getInteger("Damage");
            int maxDefault = itemStack.getType().getMaxDurability();
            int durability;
            durability = maxDurabilityCustom - Damage * maxDurabilityCustom / maxDefault;
            container = meta.getPersistentDataContainer();
            container.set(durabilityMechanicNamespace, PersistentDataType.INTEGER, durability);
            itemStack.setItemMeta(meta);
        }
    }

    private void ChangeCustomDurability(ItemStack item, int damage) {
        ItemMeta meta = item.getItemMeta();
        NamespacedKey durabilityMechanicNamespace = new NamespacedKey(plugin, "durability");
        NamespacedKey maxCustomDurability = new NamespacedKey(plugin, "maxCustomDurability");

        if (meta == null)
            return;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(durabilityMechanicNamespace, PersistentDataType.INTEGER))
            return;

        int durability = container.get(durabilityMechanicNamespace, PersistentDataType.INTEGER);
        int maxDurabilityCustom = container.get(maxCustomDurability, PersistentDataType.INTEGER);
        durability += damage;
        if (durability <= 0) {
            item.setAmount(0);
            return;
        }
        if (durability > maxDurabilityCustom) {
            maxDurabilityCustom = durability;
            container.set(maxCustomDurability, PersistentDataType.INTEGER, maxDurabilityCustom);
        }

        container.set(durabilityMechanicNamespace, PersistentDataType.INTEGER, durability);
        item.setItemMeta(meta);
        NBTItem nbtItem = new NBTItem(item);

        int d = item.getType().getMaxDurability() - item.getType().getMaxDurability() * durability / maxDurabilityCustom;


        nbtItem.setInteger("Damage", d);
        nbtItem.applyNBT(item);
    }


    //DONE
    @EventHandler
    public void durabilityEvent(PlayerItemDamageEvent e) {
        ChangeCustomDurability(e.getItem(), -e.getDamage());
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
        ItemMeta meta = event.getItem().getItemMeta();

        NamespacedKey empireID = new NamespacedKey(plugin, "id");
        String id = meta.getPersistentDataContainer().get(empireID, PersistentDataType.STRING);
        List<InteractEvent> events = item_events.get(id);//Получаем эвент конкретного предмета
        Player player = event.getPlayer();
        if (events != null)
            for (InteractEvent ev : events)
                if (ev.click.equalsIgnoreCase("drink"))
                    HandleEvent(ev, player);
                else if (ev.click.equalsIgnoreCase("eat"))
                    HandleEvent(ev, player);


    }
}
