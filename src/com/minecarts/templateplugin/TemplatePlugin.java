package com.minecarts.templateplugin;

import org.bukkit.plugin.PluginManager;

import org.bukkit.event.*;

import com.minecarts.templateplugin.command.TestCommand;
import com.minecarts.templateplugin.listener.*;

public class TemplatePlugin extends org.bukkit.plugin.java.JavaPlugin{
    
	//Listeners
	private final PlayerListener playerListener = new PlayerListener();
    
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();

        //Register events
        pm.registerEvent(Event.Type.PLAYER_CHAT, this.playerListener, Event.Priority.Highest, this);
        
        //Register commands
        getCommand("test").setExecutor(new TestCommand(this));
    }
    
    public void onDisable(){
        
    }
}
