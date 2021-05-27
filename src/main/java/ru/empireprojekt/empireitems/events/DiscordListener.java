package ru.empireprojekt.empireitems.events;


import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePostProcessEvent;
import github.scarsz.discordsrv.api.events.GameChatMessagePreProcessEvent;
import github.scarsz.discordsrv.hooks.vanish.EssentialsHook;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import ru.empireprojekt.empireitems.EmpireItems;

import java.util.ArrayList;
import java.util.List;

public class DiscordListener implements Listener {

    private EmpireItems plugin;
    private List<String> messages;
    private static Chat chat = null;

    DiscordListener(EmpireItems plugin) {
        this.plugin = plugin;
        //DiscordSRV.api.subscribe(this);
        messages = new ArrayList<>();
        RegisteredServiceProvider<Chat> rsp = plugin.getServer().getServicesManager().getRegistration(Chat.class);
        //chat = rsp.getProvider();
    }

    void onDisable() {
        //DiscordSRV.api.unsubscribe(this);
    }

    List<String> getMessages() {
        return messages;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        //chat.setPlayerPrefix(event.getPlayer(),plugin.CONSTANTS.GetEmoji(chat.getPlayerPrefix(event.getPlayer())));
//        String msg = event.getMessage();
//        String newMessage = msg + " ";
//            messages.add(newMessage);
//        event.setMessage(plugin.CONSTANTS.GetEmoji(msg));
    }


//    @Subscribe(priority = ListenerPriority.MONITOR)
//    public void onChatMessageFromDiscord(DiscordGuildMessagePostProcessEvent event) { // From Discord to in-game
//        String message = event.getProcessedMessage();
//        event.setProcessedMessage(plugin.CONSTANTS.GetEmoji(message));
//    }
//
//    @Subscribe(priority = ListenerPriority.HIGHEST)
//    public void onChatMessageFromInGame(GameChatMessagePreProcessEvent event) { // From in-game to Discord
//
//        int count = getMessages().size();
//        for (int i = 0; i < count; i++) {
//            String msg = getMessages().get(0);
//            event.setMessage(msg);
//            getMessages().remove(0);
//        }
//    }
}
