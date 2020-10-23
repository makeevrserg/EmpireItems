package ru.empireprojekt.empireitems.ItemManager;

public class mAttribute {
    public String name;
    public double amount=0.0;
    public String equipment_slot="HAND";

    public mAttribute(String name, double amount, String equipment_slot) {
        this.name = name;
        this.amount = amount;
        this.equipment_slot = equipment_slot;
    }

    public void printAtr(){
        System.out.println("name:"+name+";slot:"+equipment_slot+"amount"+amount);
    }
}
