package ru.empireprojekt.empireitems.events;

import de.tr7zw.nbtapi.NBTItem;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GenericListener implements Listener {
    private Map<String, InteractEvent[]> item_events;
    private EmpireItems plugin;
    private EmpireConstants CONSTANTS;


    private ArrayList<Location> blockLocations;

    private List<String> messages = new ArrayList<>();

    private DiscordListener discordListener;

    public GenericListener(EmpireItems plugin, Map<String, InteractEvent[]> item_events) {
        this.plugin = plugin;
        this.CONSTANTS = plugin.CONSTANTS;
        this.item_events = item_events;
        blockLocations = new ArrayList<>();
        if (plugin.getServer().getPluginManager().getPlugin("DiscordSRV") != null)
            discordListener = new DiscordListener(plugin);
        else
            System.out.println(ChatColor.AQUA + "[EmpireItems]" + ChatColor.YELLOW + "DiscordSRV Is not installed!");
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
        if (discordListener != null)
            discordListener.onDisable();
        messages = new ArrayList<>();
        blockLocations = new ArrayList<>();

    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        Projectile projectile = e.getEntity();
        if (projectile instanceof Snowball) {
            ItemStack itemStack = ((Snowball) projectile).getItem();
            ItemMeta meta = itemStack.getItemMeta();
            NamespacedKey itemExplosionNamespace = new NamespacedKey(plugin, "onHitGroundExplosion");
            if (meta == null)
                return;
            Integer explosionPower = meta.getPersistentDataContainer().get(itemExplosionNamespace, PersistentDataType.INTEGER);
            if (explosionPower != null) {
                projectile.getWorld().createExplosion(projectile.getLocation(), explosionPower);
            }

        }
    }




    List<String> getMessages(){
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
            if (meta != null && meta.getPersistentDataContainer().has(CONSTANTS.empireID,PersistentDataType.STRING)) {
                String id = meta.getPersistentDataContainer().get(CONSTANTS.empireID, PersistentDataType.STRING);
                InteractEvent[] events = item_events.get(id);//Получаем эвент конкретного предмета
                if (events != null)
                    for (InteractEvent ev : events)
                        if (ev.click.equalsIgnoreCase("entity_damage"))
                            HandleEvent(ev, player);
            }

        }
    }


    public void PlaySplash(Player p, String splash) {
        ItemStack totemItem = plugin.items.get(splash);
        if (p == null || totemItem == null) {
            System.out.println(ChatColor.AQUA + "[EmpireItems]" + "Splash отсутствует либо игрок=null!: " + splash);
            return;
        }
        ItemStack itemInHand = p.getInventory().getItemInMainHand().clone();
        p.getInventory().setItemInMainHand(totemItem);
        p.playEffect(EntityEffect.TOTEM_RESURRECT);
        p.getInventory().setItemInMainHand(itemInHand);
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
    public void repairEvent(PlayerItemMendEvent e) {
        ChangeCustomDurability(e.getItem(), +e.getRepairAmount());
    }


    private void ReplaceLore(List<String> fromReplace, String toReplace, String key) {
        for (int i = 0; i < fromReplace.size(); ++i)
            if (fromReplace.get(i).contains(key))
                //noinspection SuspiciousListRemoveInLoop
                fromReplace.remove(i);

        fromReplace.add(toReplace);


    }


    private void CheckContainer(List<String> lore, PersistentDataContainer container, NamespacedKey namespacedKey, String attrName) {
        if (!container.has(namespacedKey, PersistentDataType.DOUBLE))
            return;
        String attr = getStrAttr(attrName);
        @SuppressWarnings("ConstantConditions")
        double amount = container.get(namespacedKey, PersistentDataType.DOUBLE);

        String color = getAttrColor(amount);
        if (amount < 0)
            ReplaceLore(lore, plugin.CONSTANTS.HEXPattern(color + attr + ' ' + amount), attr);
        else
            ReplaceLore(lore, plugin.CONSTANTS.HEXPattern(color + attr + " +" + amount), attr);

    }

    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent e) {
        if (e.getInventory() instanceof AnvilInventory) {
            InventoryView view = e.getView();
            int rawSlot = e.getRawSlot();
            if (rawSlot == view.convertSlot(rawSlot)) {
                if (rawSlot == 2) {
                    ItemStack item = e.getCurrentItem();
                    if (item != null) {
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            PersistentDataContainer container = meta.getPersistentDataContainer();
                            if (container.has(CONSTANTS.empireID, PersistentDataType.STRING)) {
                                List<String> lore = meta.getLore();
                                if (lore == null)
                                    lore = new ArrayList<>();
                                CheckContainer(lore, container, CONSTANTS.GENERIC_ATTACK_DAMAGE, "GENERIC_ATTACK_DAMAGE");
                                CheckContainer(lore, container, CONSTANTS.GENERIC_ATTACK_SPEED, "GENERIC_ATTACK_SPEED");
                                CheckContainer(lore, container, CONSTANTS.GENERIC_ATTACK_KNOCKBACK, "GENERIC_ATTACK_KNOCKBACK");
                                CheckContainer(lore, container, CONSTANTS.GENERIC_ARMOR, "GENERIC_ARMOR");
                                CheckContainer(lore, container, CONSTANTS.GENERIC_ARMOR_TOUGHNESS, "GENERIC_ARMOR_TOUGHNESS");
                                CheckContainer(lore, container, CONSTANTS.GENERIC_KNOCKBACK_RESISTANCE, "GENERIC_KNOCKBACK_RESISTANCE");
                                CheckContainer(lore, container, CONSTANTS.GENERIC_LUCK, "GENERIC_LUCK");
                                CheckContainer(lore, container, CONSTANTS.GENERIC_MAX_HEALTH, "GENERIC_MAX_HEALTH");
                                CheckContainer(lore, container, CONSTANTS.GENERIC_MOVEMENT_SPEED, "GENERIC_MOVEMENT_SPEED");


                                meta.setLore(lore);
                                item.setItemMeta(meta);
                            }
                        }
                    }
                }
            }
        }
    }

    private String getAttrColor(double amount) {
        if (amount < 1)
            return "#d4311c";
        else if (amount < 5)
            return "#d4781c";
        else if (amount < 10)
            return "#d4d41c";
        else if (amount < 15)
            return "#7ed41c";
        else if (amount < 20)
            return "#2ed41c";
        else
            return "#1ccbd4";
    }

    public static String getStrAttr(String str) {
        if (str.equalsIgnoreCase("GENERIC_ATTACK_SPEED"))
            return "Скорость атаки";
        else if (str.equalsIgnoreCase("GENERIC_ATTACK_KNOCKBACK"))
            return "Откидывание";
        else if (str.equalsIgnoreCase("GENERIC_ATTACK_DAMAGE"))
            return "Урон";
        else if (str.equalsIgnoreCase("GENERIC_ARMOR"))
            return "Броня";
        else if (str.equalsIgnoreCase("GENERIC_ARMOR_TOUGHNESS"))
            return "Прочность брони";
        else if (str.equalsIgnoreCase("GENERIC_KNOCKBACK_RESISTANCE"))
            return "Сопротивление откидыванию";
        else if (str.equalsIgnoreCase("GENERIC_MAX_HEALTH"))
            return "Здоровье";
        else if (str.equalsIgnoreCase("GENERIC_LUCK"))
            return "Удача";
        else if (str.equalsIgnoreCase("GENERIC_MOVEMENT_SPEED"))
            return "Скорость";
        return "";
    }

    private void AddAmountToContainer(String atrName, String atrToCompaer, NamespacedKey namespacedKey, PersistentDataContainer oldItemContainer, PersistentDataContainer resultContainer, double amount) {
        if (atrName.equalsIgnoreCase(atrToCompaer)) {
            Double oldValue = oldItemContainer.get(namespacedKey, PersistentDataType.DOUBLE);
            amount += (oldValue == null) ? 0 : oldValue;
            amount = Math.round(amount * 10.0) / 10.0;
            resultContainer.set(namespacedKey, PersistentDataType.DOUBLE, amount);
        }
    }

    @EventHandler
    public void anvilEvent(PrepareAnvilEvent e) {

        if (plugin.mSettings.isUpgradeEnabled) {
            ItemStack itemBefore = e.getInventory().getItem(0);
            ItemStack ingredient = e.getInventory().getItem(1);

            if (itemBefore != null && ingredient != null) {
                ItemMeta itemMetaBefore = itemBefore.getItemMeta();
                if (itemMetaBefore != null && e.getInventory().getSize() > 1) {
                    ItemMeta ingrMeta = ingredient.getItemMeta();
                    //Check on Upgrade
                    if (ingrMeta != null) {
                        PersistentDataContainer oldItemContainer = itemMetaBefore.getPersistentDataContainer();
                        PersistentDataContainer ingridientContainer = ingrMeta.getPersistentDataContainer();
                        int upgradeCount = ingredient.getAmount();
                        if (oldItemContainer.has(CONSTANTS.ITEM_UPGRADE_COUNT, PersistentDataType.INTEGER))
                            //noinspection ConstantConditions
                            upgradeCount += oldItemContainer.get(CONSTANTS.ITEM_UPGRADE_COUNT, PersistentDataType.INTEGER);


                        if (oldItemContainer.has(CONSTANTS.empireID, PersistentDataType.STRING) && ingridientContainer.has(CONSTANTS.empireID, PersistentDataType.STRING)) {
                            String ingrId = ingridientContainer.get(CONSTANTS.empireID, PersistentDataType.STRING);
                            ItemStack result = itemBefore.clone();
                            ItemMeta resultMeta = result.getItemMeta();
                            assert resultMeta != null;
                            PersistentDataContainer resultContainer = resultMeta.getPersistentDataContainer();
                            //Если ингридиент снижает стоимость апгрейда
                            if (plugin.itemUpgradeCostDecreaser.containsKey(ingrId)) {
                                if (oldItemContainer.has(CONSTANTS.ITEM_UPGRADE_COUNT, PersistentDataType.INTEGER))
                                    //noinspection ConstantConditions
                                    upgradeCount -= oldItemContainer.get(CONSTANTS.ITEM_UPGRADE_COUNT, PersistentDataType.INTEGER);
                                int upgradeDecrease = (int) (plugin.itemUpgradeCostDecreaser.get(ingrId) * (double) ingredient.getAmount());
                                upgradeCount -= upgradeDecrease;
                                if (upgradeCount <= 0)
                                    upgradeCount = 1;
                                resultContainer.set(CONSTANTS.ITEM_UPGRADE_COUNT, PersistentDataType.INTEGER, upgradeCount);
                                result.setItemMeta(resultMeta);
                                e.setResult(result);
                                e.getInventory().setRepairCost((ingredient.getAmount()));
                                plugin.getServer().getScheduler().runTask(plugin, () -> e.getInventory().setRepairCost(ingredient.getAmount()));


                            } else if (plugin.itemUpgradesInfo.containsKey(ingridientContainer.get(CONSTANTS.empireID, PersistentDataType.STRING))) {
                                resultContainer.set(CONSTANTS.ITEM_UPGRADE_COUNT, PersistentDataType.INTEGER, upgradeCount);
                                upgradeCount -= ingredient.getAmount();
                                for (EmpireItems.itemUpgradeClass iUpg : plugin.itemUpgradesInfo.get(ingrId)) {
                                    double amount = (iUpg.min_add +  (Math.random() * (iUpg.max_add - iUpg.min_add))) * ingredient.getAmount();
                                    if (itemBefore.getType().toString().toLowerCase().contains("helmet"))
                                        iUpg.slot = EquipmentSlot.HEAD.name();
                                    if (itemBefore.getType().toString().toLowerCase().contains("leggings"))
                                        iUpg.slot = EquipmentSlot.LEGS.name();
                                    if (itemBefore.getType().toString().toLowerCase().contains("chestplate"))
                                        iUpg.slot = EquipmentSlot.CHEST.name();
                                    if (itemBefore.getType().toString().toLowerCase().contains("boots"))
                                        iUpg.slot = EquipmentSlot.FEET.name();

                                    if (itemBefore.getType().equals(Material.BOW))
                                        return;

                                    if (itemBefore.getType().toString().toLowerCase().contains("sword") || itemBefore.getType().toString().toLowerCase().contains("axe")) {
                                        AddAmountToContainer(iUpg.attribute, "GENERIC_ATTACK_DAMAGE", CONSTANTS.GENERIC_ATTACK_DAMAGE, oldItemContainer, resultContainer, amount);
                                        AddAmountToContainer(iUpg.attribute, "GENERIC_ATTACK_SPEED", CONSTANTS.GENERIC_ATTACK_SPEED, oldItemContainer, resultContainer, amount);
                                        AddAmountToContainer(iUpg.attribute, "GENERIC_ATTACK_KNOCKBACK", CONSTANTS.GENERIC_ATTACK_KNOCKBACK, oldItemContainer, resultContainer, amount);

                                        if (iUpg.attribute.equalsIgnoreCase("GENERIC_ARMOR") ||
                                                iUpg.attribute.equalsIgnoreCase("GENERIC_ARMOR_TOUGHNESS") ||
                                                iUpg.attribute.equalsIgnoreCase("GENERIC_LUCK") ||
                                                iUpg.attribute.equalsIgnoreCase("GENERIC_MAX_HEALTH") ||
                                                iUpg.attribute.equalsIgnoreCase("GENERIC_KNOCKBACK_RESISTANCE") ||
                                                iUpg.attribute.equalsIgnoreCase("GENERIC_MOVEMENT_SPEED")
                                        )
                                            continue;

                                    }
                                    if (itemBefore.getType().toString().toLowerCase().contains("helmet") ||
                                            itemBefore.getType().toString().toLowerCase().contains("chestplate") ||
                                            itemBefore.getType().toString().toLowerCase().contains("leggings") ||
                                            itemBefore.getType().toString().toLowerCase().contains("boots") ||
                                            itemBefore.getType().equals(Material.SHIELD)
                                    ) {
                                        AddAmountToContainer(iUpg.attribute, "GENERIC_ARMOR", CONSTANTS.GENERIC_ARMOR, oldItemContainer, resultContainer, amount);
                                        AddAmountToContainer(iUpg.attribute, "GENERIC_ARMOR_TOUGHNESS", CONSTANTS.GENERIC_ARMOR_TOUGHNESS, oldItemContainer, resultContainer, amount);
                                        AddAmountToContainer(iUpg.attribute, "GENERIC_LUCK", CONSTANTS.GENERIC_LUCK, oldItemContainer, resultContainer, amount);
                                        AddAmountToContainer(iUpg.attribute, "GENERIC_MAX_HEALTH", CONSTANTS.GENERIC_MAX_HEALTH, oldItemContainer, resultContainer, amount);
                                        AddAmountToContainer(iUpg.attribute, "GENERIC_KNOCKBACK_RESISTANCE", CONSTANTS.GENERIC_KNOCKBACK_RESISTANCE, oldItemContainer, resultContainer, amount);
                                        AddAmountToContainer(iUpg.attribute, "GENERIC_MOVEMENT_SPEED", CONSTANTS.GENERIC_MOVEMENT_SPEED, oldItemContainer, resultContainer, amount);
                                        if (iUpg.attribute.equalsIgnoreCase("GENERIC_ATTACK_DAMAGE") ||
                                                iUpg.attribute.equalsIgnoreCase("GENERIC_ATTACK_SPEED") ||
                                                iUpg.attribute.equalsIgnoreCase("GENERIC_ATTACK_KNOCKBACK")
                                        )
                                            continue;
                                    }
                                    if (itemBefore.getType().equals(Material.SHIELD))
                                        iUpg.slot = EquipmentSlot.HAND.name();
                                    AttributeModifier attributeModifier = new AttributeModifier(UUID.randomUUID(), iUpg.attribute, amount, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.valueOf(iUpg.slot));
                                    resultMeta.addAttributeModifier(
                                            Attribute.valueOf(iUpg.attribute),
                                            attributeModifier
                                    );

                                    if (itemBefore.getType().equals(Material.SHIELD)) {
                                        iUpg.slot = EquipmentSlot.OFF_HAND.name();
                                        attributeModifier = new AttributeModifier(UUID.randomUUID(), iUpg.attribute, amount, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.valueOf(iUpg.slot));
                                        resultMeta.addAttributeModifier(
                                                Attribute.valueOf(iUpg.attribute),
                                                attributeModifier
                                        );
                                    }
                                    List<String> resultLore = resultMeta.getLore();
                                    if (resultLore == null)
                                        resultLore = new ArrayList<>();
                                    String attr = getStrAttr(iUpg.attribute);
                                    if (ingredient.getAmount() == 1)
                                        ReplaceLore(resultLore, plugin.CONSTANTS.HEXPattern(ChatColor.RED + attr + " +" + ChatColor.MAGIC + "-1"), attr);
                                    else
                                        ReplaceLore(resultLore, plugin.CONSTANTS.HEXPattern(ChatColor.RED + attr + " +" + ChatColor.MAGIC + "-1" + ChatColor.RED + "x" + ingredient.getAmount()), attr);

                                    resultMeta.setLore(resultLore);

                                }
                                result.setItemMeta(resultMeta);
                                e.setResult(result);
                                int finalUpgradeCount = upgradeCount;
                                e.getInventory().setRepairCost((int) ((ingredient.getAmount() / 2 + finalUpgradeCount) * plugin.mSettings.upgradeCostMultiplier));
                                plugin.getServer().getScheduler().runTask(plugin, () -> e.getInventory().setRepairCost((int) ((ingredient.getAmount() / 2 + finalUpgradeCount) * plugin.mSettings.upgradeCostMultiplier)));
                            }

                        }
                    }
                }
            }
        }


        if (e.getResult() != null) {


            //Check on Experience
            ItemStack itemStack = e.getResult();
            ItemMeta resultMeta = itemStack.getItemMeta();
            if (resultMeta == null)
                return;
            PersistentDataContainer container = resultMeta.getPersistentDataContainer();
            int Damage;
            if (container.has(CONSTANTS.durabilityMechanicNamespace, PersistentDataType.INTEGER)) {
                @SuppressWarnings("ConstantConditions")
                int maxDurabilityCustom = container.get(CONSTANTS.maxCustomDurability, PersistentDataType.INTEGER);
                if (plugin.getServer().getPluginManager().getPlugin("Item-NBT-API") != null) {
                    NBTItem nbtItem = new NBTItem(itemStack);
                    Damage = nbtItem.getInteger("Damage");
                } else //noinspection deprecation
                    Damage = itemStack.getDurability();
                int maxDefault = itemStack.getType().getMaxDurability();
                int durability;
                durability = maxDurabilityCustom - Damage * maxDurabilityCustom / maxDefault;
                container = resultMeta.getPersistentDataContainer();
                container.set(CONSTANTS.durabilityMechanicNamespace, PersistentDataType.INTEGER, durability);
                itemStack.setItemMeta(resultMeta);
            }
        }
    }


    private void ChangeCustomDurability(ItemStack item, int damage) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        Integer durability = container.get(CONSTANTS.durabilityMechanicNamespace, PersistentDataType.INTEGER);
        if (durability == null)
            return;

        @SuppressWarnings("ConstantConditions")
        int maxDurabilityCustom = container.get(CONSTANTS.maxCustomDurability, PersistentDataType.INTEGER);
        durability += damage;
        if (durability <= 0) {
            item.setAmount(0);
            return;
        }
        if (durability > maxDurabilityCustom) {
            maxDurabilityCustom = durability;
            container.set(CONSTANTS.maxCustomDurability, PersistentDataType.INTEGER, maxDurabilityCustom);
        }

        container.set(CONSTANTS.durabilityMechanicNamespace, PersistentDataType.INTEGER, durability);
        item.setItemMeta(meta);
        if (plugin.getServer().getPluginManager().getPlugin("Item-NBT-API") != null) {
            NBTItem nbtItem = new NBTItem(item);
            int d = item.getType().getMaxDurability() - item.getType().getMaxDurability() * durability / maxDurabilityCustom;
            nbtItem.setInteger("Damage", d);
            nbtItem.applyNBT(item);
        }
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
