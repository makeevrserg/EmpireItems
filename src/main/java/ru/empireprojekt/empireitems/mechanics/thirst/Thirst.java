package ru.empireprojekt.empireitems.mechanics.thirst;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.empireprojekt.empireitems.EmpireItems;
import ru.empireprojekt.empireitems.files.DataManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Thirst implements Listener, CommandExecutor {
    EmpireItems plugin;
    public EmpireItems.PluginSettings mSettings;
    DataManager thirstSettingsManager;
    Thread thirstThread = null;
    boolean thirstAlive = true;
    Map<Player,Double> thirsPlayerMap;


    public Thirst(EmpireItems plugin, EmpireItems.PluginSettings mSettings) {
        this.plugin = plugin;
        this.mSettings = mSettings;
        plugin.getCommand("thirst").setExecutor(this);
        this.thirsPlayerMap = new HashMap<>();
        if (!Bukkit.getOnlinePlayers().isEmpty())
            for (Player player : Bukkit.getOnlinePlayers())
                thirsPlayerMap.put(player,10.0);
        InitThirst();
        plugin.getServer().getPluginManager().registerEvents(this,plugin);

    }

    private String getThirst(double amountF, Player player) {
        System.out.println(amountF);
        String thirst_full = thirstSettingsManager.getConfig().getString("thirst_full");
        String thirst_blank = thirstSettingsManager.getConfig().getString("thirst_blank");
        String thirst_half = thirstSettingsManager.getConfig().getString("thirst_half");
        String offset = thirstSettingsManager.getConfig().getString("thirst_offset");
        int max_thirst = thirstSettingsManager.getConfig().getInt("max_thirst");
        int amountI = (int) amountF;
        String[] strL = new String[10];
        Arrays.fill(strL, thirst_blank);
        for (int i =strL.length-1;i>strL.length-amountI-1;--i)
            strL[i] = thirst_full;

        if (Math.abs(amountF-amountI)>0.3 && Math.abs(amountF-amountI)<0.7)
            strL[strL.length-amountI-1] = thirst_half;

        StringBuilder builder = new StringBuilder();
        for (String value : strL)
            builder.append(value);



        return builder.toString()+offset;
    }

    private void InitThirst() {
        this.thirstSettingsManager = new DataManager("thirst/config.yml", plugin);
        thirstAlive = true;
        thirstThread = new Thread(() -> {
            while (thirstAlive) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!Bukkit.getOnlinePlayers().isEmpty())
                    for (Player player : Bukkit.getOnlinePlayers())
                        createBar(player);
            }

        });
        thirstThread.start();


    }

    //Jump Move Attack
    @EventHandler
    private void onJump(PlayerJumpEvent e){
        Player player = e.getPlayer();
        double value = thirsPlayerMap.get(player)-0.5;
        value = (value<0)?0:value;
        thirsPlayerMap.put(player,value);
    }

    public void disable() {
        PlayerJoinEvent.getHandlerList().unregister(this);
        thirstAlive=false;
        thirstThread.interrupt();
        thirstThread.stop();
    }

    private void createBar(Player player) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                getThirst(thirsPlayerMap.get(player),
                        player)
        ));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        createBar(event.getPlayer());
        thirsPlayerMap.put(event.getPlayer(),10.0);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("thirst"))
            if (args[0].equalsIgnoreCase("reload")) {
                thirstAlive = false;
                thirstThread.stop();
                InitThirst();
            }

        return false;
    }
}
