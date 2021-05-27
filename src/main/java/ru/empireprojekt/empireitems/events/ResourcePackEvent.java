package ru.empireprojekt.empireitems.events;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import ru.empireprojekt.empireitems.EmpireItems;

public class ResourcePackEvent implements Listener {
    EmpireItems plugin;

    public ResourcePackEvent(EmpireItems plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoinEvent(PlayerJoinEvent e) {
        if (!plugin.mSettings.downloadResourcePackOnJoin)
            return;
        Player p = e.getPlayer();
        p.performCommand("empack");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        p.sendTitle("Скачайте ресурс-пак","Введите /empack.",5,200,5);
    }
    @EventHandler
    public void onRespawn(PlayerRespawnEvent e){
        Player p = e.getPlayer();
        p.sendTitle("Скачайте ресурс-пак","Введите /empack. Он нужен для новых предметов, звуков, оружие, брони и новых меню в /menu",5,200,5);

    }

    @EventHandler
    public void onResourcePack(PlayerResourcePackStatusEvent e) {
        Player p = e.getPlayer();
        System.out.println(e.getStatus().name());

        if (e.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED) {
            p.sendMessage(plugin.CONSTANTS.PLUGIN_MESSAGE + ChatColor.RED + "Вы отклонили ресурс-пак.");
            p.sendMessage(plugin.CONSTANTS.PLUGIN_MESSAGE + ChatColor.AQUA + " Попробуйте скачать самостоятельно. empireprojekt.ru/files/EmpireProjektPack.zip");

        } else if (e.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
            p.kickPlayer(ChatColor.RED+"Не удалось загрузить ресурс-пак\n" +
                    ChatColor.GREEN+"Перезайдите и введите /empack ещё раз\n" +
                    ChatColor.AQUA+"Или скачайте его самостоятельно с сайта EmpireProjekt.ru\n" +
                    "Дискорд: EmpireProjekt.ru/discord");
            p.sendMessage(plugin.CONSTANTS.PLUGIN_MESSAGE + ChatColor.RED + " Не удалось загрузить ресурс-пак.");
            p.sendMessage(plugin.CONSTANTS.PLUGIN_MESSAGE + ChatColor.AQUA + " Попробуйте скачать самостоятельно либо введите /empack ещё раз. empireprojekt.ru/files/EmpireProjektPack.zip");

        }

    }

}
