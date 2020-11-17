package ru.empireprojekt.empireitems;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabCompletition implements TabCompleter {
    List<String> items;
    public TabCompletition(List<String> list){
        items=list;
    }
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        List<String> arguments = new ArrayList<String>();
        if (args.length==0){
            arguments = new ArrayList<String>();
            arguments.add("emp");
            arguments.add("empireitems");
            return arguments;

        }
        else if (args.length==1){
            arguments = new ArrayList<String>();
            arguments.add("reload");
            arguments.add("give");
            return arguments;
        }
        else if (args.length==2){
            if (args[0].equalsIgnoreCase("give")){
                return items;
            }
        }
        return null;
    }
}
