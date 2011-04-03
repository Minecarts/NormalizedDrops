package com.minecarts.templateplugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.minecarts.templateplugin.*;

public class TestCommand extends CommandHandler{
    
    public TestCommand(TemplatePlugin plugin){
        super(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("A deafult command");
        return true;
    }
}
