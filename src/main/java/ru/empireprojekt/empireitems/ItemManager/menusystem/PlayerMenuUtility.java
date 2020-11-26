package ru.empireprojekt.empireitems.ItemManager.menusystem;

import org.bukkit.entity.Player;

public class PlayerMenuUtility {
    private Player player;

    public PlayerMenuUtility(Player player) {
        this.player = player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }
}
