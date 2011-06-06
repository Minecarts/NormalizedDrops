package com.minecarts.normalizeddrops.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import com.minecarts.normalizeddrops.*;

public class MainCommand extends CommandHandler{
    
    public MainCommand(NormalizedDrops plugin){
        super(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 1 && args[0].equalsIgnoreCase("reload")){
            //Reload the config
            if(sender.isOp() || sender instanceof ConsoleCommandSender){
                plugin.entityListener.setConfig(plugin.getConfiguration());
                sender.sendMessage(plugin.pdf.getName() + " configuration reloaded.");
            }
            return true;
        }
        return false;
    }
}
