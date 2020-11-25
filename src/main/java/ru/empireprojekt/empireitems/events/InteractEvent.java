package ru.empireprojekt.empireitems.events;

import org.bukkit.ChatColor;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.empireprojekt.empireitems.ItemManager.mPotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class InteractEvent {

    public String click = null;//RIGHT_CLICK; LEFT_CLICK
    public String play_particle = null;
    public int particle_count = 1;
    public double particle_time = 0.2;
    public String play_sound = null;
    public int sound_volume = 1;
    public int sound_pitch = 1;
    public boolean as_console=false;
    public List<String> execute_commands;
    public Collection<PotionEffect> potion_effects;
    public List<PotionEffectType> remove_potion_effect;
    public int explosionPower = -1;
    public InteractEvent() {
        execute_commands = new ArrayList<String>();
        potion_effects = new ArrayList<PotionEffect>();
        remove_potion_effect = new ArrayList<PotionEffectType>();
    }

    public InteractEvent(String click, String play_particle, int particle_count, double particle_time, String play_sound) {
        this.click = click;
        this.play_particle = play_particle;
        this.particle_count = particle_count;
        this.particle_time = particle_time;
        this.play_sound = play_sound;
    }
    public void PrintEvents(){
        System.out.println(ChatColor.GREEN+"----------------------EVENTS-------------------");
        System.out.println("Events: click " +click+
                "\nplay_particle " +play_particle+
                "\nparticle_count " +particle_count+
                "\nparticle_time " +particle_time+
                "\nplay_sound " +play_sound+
                "\nsound_volume " +sound_volume+
                "\nsound_pitch " +sound_pitch+
                "\nas_console " + as_console +
                "\nexecute_commands " + Arrays.toString(execute_commands.toArray()) +
                "\npotion_effects " + Arrays.toString(potion_effects.toArray()) +
                "\nremove_potion_effect " + Arrays.toString(remove_potion_effect.toArray())

        );
        System.out.println(ChatColor.GREEN+"-----------------------------------------------");



    }
}
