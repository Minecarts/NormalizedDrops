package com.minecarts.normalizeddrops;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.MessageFormat;

import org.bukkit.configuration.file.FileConfiguration;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;

import org.bukkit.plugin.PluginManager;

import org.bukkit.event.Event;

import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Projectile;


public class NormalizedDrops extends org.bukkit.plugin.java.JavaPlugin {
    private static final Logger logger = Logger.getLogger("com.minecarts.normalizeddrops");
    
    protected boolean debug;
    
    
    public void onEnable() {
        reloadConfig();
        
        // internal plugin commands
        getCommand("normalizeddrops").setExecutor(new CommandExecutor() {
            public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
                if(!sender.hasPermission("normalizeddrops.reload")) return true; // "hide" command output for nonpermissibles
                
                if(args[0].equalsIgnoreCase("reload")) {
                    NormalizedDrops.this.reloadConfig();
                    sender.sendMessage("NormalizedDrops config reloaded.");
                    return true;
                }
                
                return false;
            }
        });
        
        
        PluginManager pluginManager = getServer().getPluginManager();
        // create listeners
        EntityListener entityListener = new EntityListener(this);
        // register events
        pluginManager.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Event.Priority.Normal, this);
        pluginManager.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.Monitor, this);
        
        
        log("Version {0} enabled.", getDescription().getVersion());
    }
    
    public void onDisable() {
    }
    
    
    public Player getParentPlayer(Object entity) {
        if(entity instanceof Player) {
            return (Player) entity;
        }
        
        if(entity instanceof Tameable) {
            return getParentPlayer(((Tameable) entity).getOwner());
        }
        
        if(entity instanceof Projectile) {
            return getParentPlayer(((Projectile) entity).getShooter());
        }
        
        return null;
    }
    
    
    @Override
    public void reloadConfig() {
        super.reloadConfig();
        final FileConfiguration config = getConfig();
        
        debug = config.getBoolean("debug");
    }
    
    
    public void log(String message) {
        log(Level.INFO, message);
    }
    public void log(Level level, String message) {
        logger.log(level, MessageFormat.format("{0}> {1}", getDescription().getName(), message));
    }
    public void log(String message, Object... args) {
        log(MessageFormat.format(message, args));
    }
    public void log(Level level, String message, Object... args) {
        log(level, MessageFormat.format(message, args));
    }
    
    public void debug(String message) {
        if(debug) log(message);
    }
    public void debug(String message, Object... args) {
        if(debug) log(message, args);
    }
}
