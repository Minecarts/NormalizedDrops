package com.minecarts.normalizeddrops;

import java.util.logging.Logger;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.PluginDescriptionFile;

import org.bukkit.event.*;
import org.bukkit.util.config.Configuration;

import com.minecarts.normalizeddrops.command.MainCommand;
import com.minecarts.normalizeddrops.listener.*;

public class NormalizedDrops extends org.bukkit.plugin.java.JavaPlugin{
    
    public final Logger log = Logger.getLogger("com.minecarts.normalizeddrops");
    public EntityListener entityListener;
    public Configuration config;
    public PluginDescriptionFile pdf;

    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        this.pdf = getDescription();
        
        //Load config
        this.config = getConfiguration();

        //Listeners
        this.entityListener = new EntityListener();
        this.entityListener.setConfigValues(config);

        //Events
        pm.registerEvent(Event.Type.ENTITY_DEATH, this.entityListener, Event.Priority.Normal, this);

        //Commands
        getCommand("ndrop").setExecutor(new MainCommand(this));

        log.info("[" + pdf.getName() + "] version " + pdf.getVersion() + " enabled.");
    }
    
    public void onDisable(){
        
    }
}
