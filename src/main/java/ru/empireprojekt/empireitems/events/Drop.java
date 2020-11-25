package ru.empireprojekt.empireitems.events;

import org.bukkit.entity.Entity;

public class Drop {
    public String item;
    public int min_amount;
    public int max_amount;
    public double chance;

    public Drop(String item, int min_amount, int max_amount, double chance) {
        this.item = item;
        this.min_amount = min_amount;
        this.max_amount = max_amount;
        this.chance = chance;
    }
}
