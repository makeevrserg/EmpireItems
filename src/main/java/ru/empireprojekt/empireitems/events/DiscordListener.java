package ru.empireprojekt.empireitems.events;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePostProcessEvent;
import github.scarsz.discordsrv.api.events.GameChatMessagePreProcessEvent;
import org.bukkit.event.Listener;
import ru.empireprojekt.empireitems.EmpireItems;

public class DiscordListener implements Listener {

    private EmpireItems plugin;

    DiscordListener(EmpireItems plugin) {
        this.plugin = plugin;
        DiscordSRV.api.subscribe(this);

    }

    void onDisable() {

        DiscordSRV.api.unsubscribe(this);
    }


    @Subscribe(priority = ListenerPriority.MONITOR)
    public void onChatMessageFromDiscord(DiscordGuildMessagePostProcessEvent event) { // From Discord to in-game
        String message = event.getProcessedMessage();
        event.setProcessedMessage(plugin.CONSTANTS.GetEmoji(message, plugin.CONSTANTS.getEmojiPattern()));
    }

    @Subscribe(priority = ListenerPriority.HIGHEST)
    public void onChatMessageFromInGame(GameChatMessagePreProcessEvent event) { // From in-game to Discord
        int count = plugin.genericListener.getMessages().size();
        for (int i = 0; i < count; i++) {
            event.setMessage(plugin.genericListener.getMessages().get(0));
            plugin.genericListener.getMessages().remove(0);
        }
    }
}
