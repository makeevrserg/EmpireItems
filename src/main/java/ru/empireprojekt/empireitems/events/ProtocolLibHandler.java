package ru.empireprojekt.empireitems.events;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import ru.empireprojekt.empireitems.EmpireItems;

import javax.jws.soap.SOAPBinding;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class ProtocolLibHandler {

    EmpireItems empireItems;
    ProtocolManager protocolManager;
    PacketListener packetAdapterEmoji;

    public ProtocolLibHandler(EmpireItems empireItems) {
        this.empireItems = empireItems;
        protocolManager = ProtocolLibrary.getProtocolManager();

        empireItems.getServer().getScheduler().runTaskTimer(empireItems, () -> {
            for (Player p : empireItems.getServer().getOnlinePlayers()) {
                String formate = empireItems.mSettings.tabPrefix + p.getName();

                if (empireItems.getServer().getPluginManager().getPlugin("placeholderapi") != null)
                    formate = PlaceholderAPI.setPlaceholders(p, formate);

                formate = empireItems.CONSTANTS.HEXPattern(empireItems.CONSTANTS.GetEmoji(formate));

                p.setPlayerListName(formate);

            }


        }, 0, 40);


        packetAdapterEmoji = new PacketAdapter(empireItems, ListenerPriority.HIGHEST,
                PacketType.Play.Server.SCOREBOARD_OBJECTIVE,
                PacketType.Play.Server.SCOREBOARD_TEAM,
                PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE,
                PacketType.Play.Server.SCOREBOARD_SCORE,
                PacketType.Play.Server.PLAYER_INFO,
                PacketType.Play.Server.TITLE,
                PacketType.Play.Server.CHAT,

                PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER,
                PacketType.Play.Server.OPEN_WINDOW) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                //System.out.println("Packet Receiving: " + packet.getType().name());

            }

            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                //System.out.println("Packet Sending: " + packet.getType().name());



                for (int i = 0; i < packet.getChatComponents().size(); ++i) {
                    WrappedChatComponent chatComponent = packet.getChatComponents().read(i);

                    for (int j = 0; j < packet.getModifier().size(); ++j) {
                        Object obj = packet.getModifier().read(j);
                        if (obj == null)
                            continue;
                        if (obj instanceof TextComponent) {
                            net.kyori.adventure.text.TextComponent parentTextComponent = (net.kyori.adventure.text.TextComponent) obj;
                            parentTextComponent = parentTextComponent.children(GetChild(parentTextComponent));
                            parentTextComponent = parentTextComponent.content(empireItems.CONSTANTS.GetEmoji(parentTextComponent.content()));
                            //System.out.println("ParentComp: "+parentTextComponent.content());
                            packet.getModifier().write(j, parentTextComponent);
                        }
                    }


                    if (chatComponent == null)
                        continue;
                    JsonToUnicode(chatComponent);
                    packet.getChatComponents().setReadOnly(i, false);
                    packet.getChatComponents().write(i, chatComponent);
                    //System.out.println("ChatComp: "+chatComponent.toString());
                }


            }

        };

        protocolManager.addPacketListener(packetAdapterEmoji);
    }

    private List<TextComponent> GetChild(TextComponent component) {

        List<TextComponent> list = new ArrayList<>();
        for (Component childComponent : component.children()) {
            TextComponent childTextComponent = (TextComponent) childComponent;
            childTextComponent = childTextComponent.content(empireItems.CONSTANTS.GetEmoji(childTextComponent.content()));
            //System.out.println("ChildComp: "+childTextComponent.content());
            childTextComponent = childTextComponent.children(GetChild(childTextComponent));
            list.add(childTextComponent);
        }
        return list;
    }

    public void onDisable() {
        protocolManager.removePacketListener(packetAdapterEmoji);
    }

    private JsonObject ChangeAttr(JsonElement jEl) {
        JsonObject jObj = jEl.getAsJsonObject();
        String convertedText = empireItems.CONSTANTS.GetUi(jObj.get("text").getAsString());
        jObj.addProperty("text", convertedText);
        return jObj;
    }

    private void JsonToUnicode(WrappedChatComponent chatComponent) {
        JsonObject jsonObject = new JsonParser().parse(chatComponent.getJson()).getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("extra");
        if (jsonArray != null && jsonArray.size() > 0) {
            for (JsonElement jEl : jsonArray)
                ChangeAttr(jEl);
            jsonObject.add("extra", jsonArray);
        }

        if (jsonObject.has("text"))
            ChangeAttr(jsonObject);
        chatComponent.setJson(jsonObject.toString());

    }
}
