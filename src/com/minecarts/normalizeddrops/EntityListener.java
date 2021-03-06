package com.minecarts.normalizeddrops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.ListIterator;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;

import static org.bukkit.Material.*;



public class EntityListener implements Listener {
    
    private final NormalizedDrops plugin;
    
    private final Random random = new Random();
    private final HashMap<World, ArrayList<LocalEvent>> deathTracker = new HashMap<World, ArrayList<LocalEvent>>();
    private final HashMap<World, ArrayList<LocalEvent>> eggTracker = new HashMap<World, ArrayList<LocalEvent>>();
    private final WeakHashMap<Entity, ArrayList<EntityDamageEvent>> damageTracker = new WeakHashMap<Entity, ArrayList<EntityDamageEvent>>();
    
    public EntityListener(NormalizedDrops plugin) {
        this.plugin = plugin;
    }
    
    
    private int getNearbyCount(HashMap<World, ArrayList<LocalEvent>> tracker, Location point, double radius) {
        ArrayList<LocalEvent> events = tracker.get(point.getWorld());
        if(events == null || events.isEmpty()) return 0;
        
        int count = 0;
        // start iterator at the end of the list
        ListIterator<LocalEvent> itr = events.listIterator(events.size());
        // and interate backwards
        while(itr.hasPrevious()) {
            LocalEvent event = (LocalEvent) itr.previous();

            if(event.elapsed() > plugin.getConfig().getInt("maxAge") * 1000) {
                // event expired, so clear the list up until this point
                if(itr.previousIndex() >= 0) {
                    events.subList(0, itr.previousIndex()).clear();
                }
                break;
            }
            else {
                // check if event is within radius of point
                if(event.within(point, plugin.getConfig().getDouble("radius"))) {
                    count++;
                }
           }
        }
        
        return count;
    }
    
    private boolean normalize(HashMap<World, ArrayList<LocalEvent>> tracker, Event event, Location location) {
        int nearby = getNearbyCount(tracker, location, plugin.getConfig().getDouble("radius"));
        int r = random.nextInt(Math.max(1, plugin.getConfig().getInt("maxEvents"))) + plugin.getConfig().getInt("minEvents");

        ArrayList<LocalEvent> events = tracker.get(location.getWorld());
        if(events == null) {
            events = new ArrayList<LocalEvent>();
            tracker.put(location.getWorld(), events);
        }

        // normalization triggered
        if(nearby > r) {
            plugin.debug("Normalized at {0} (Nearby: {1} > Random: {2}, Tracker Size: {3})", location, nearby, r, events.size());
            return true;
        }
        
        // add local event to tracker
        events.add(new LocalEvent(event, location));
        return false;
    }
    
    
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void on(EntityDamageEvent event) {
        if(event.isCancelled()) return;
        
        ArrayList<EntityDamageEvent> history = damageTracker.get(event.getEntity());
        
        if(history == null) {
            history = new ArrayList<EntityDamageEvent>();
            damageTracker.put(event.getEntity(), history);
        }
        
        history.add(event);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void on(EntityDeathEvent event) {
        final Entity entity = event.getEntity();
        
        // skip normalization for player drops
        if(entity instanceof Player) return;
        
        // configurable mob types
        if(entity instanceof Animals && !plugin.getConfig().getBoolean("animals")) return;
        if(entity instanceof Monster && !plugin.getConfig().getBoolean("monsters")) return;
        
        // is this check necessary?
        // we're in the death event, so it's assumed that the entity was living
        if(entity instanceof LivingEntity) {
            // normalize items dropped
            if(event.getDrops().size() > 0) {
                if(normalize(deathTracker, event, entity.getLocation())) {
                    event.getDrops().clear();
                    event.setDroppedExp(0);
                    plugin.debug("{0} drops ({1}) and exp ({2}) cleared", entity, event.getDrops(), event.getDroppedExp());
                    return;
                }
            }
            
            // normalize experience dropped
            int exp = event.getDroppedExp();
            
            ArrayList<EntityDamageEvent> history = damageTracker.get(entity);
            damageTracker.remove(entity);
            
            if(exp > 0 && history != null) {
                int totalDamage = 0;
                int playerDamage = 0;
                
                for(EntityDamageEvent damageEvent : history) {
                    totalDamage += damageEvent.getDamage();
                    
                    if(damageEvent instanceof EntityDamageByEntityEvent) {
                        Entity attacker = ((EntityDamageByEntityEvent) damageEvent).getDamager();
                        
                        if(plugin.getParentPlayer(attacker) != null) {
                            // attacker is a player or originated from a player (projectile, tameable)
                            playerDamage += damageEvent.getDamage();
                        }
                    }
                }
                
                // normalize!
                if(totalDamage != playerDamage) {
                    int newExp = totalDamage > 0 && playerDamage > 0 ? Math.round(exp * (playerDamage / totalDamage)) : 0;
                    event.setDroppedExp(newExp);
                    plugin.debug("{0} exp drop reduced from {1} to {2} at {3}", entity, exp, newExp, entity.getLocation());
                }
            }
        }
    }
    
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void on(ItemSpawnEvent event) {
        Item item = (Item) event.getEntity();
        
        switch(item.getItemStack().getType()) {
            case EGG:
                // since ITEM_SPAWN has no causes, check if egg was laid by a nearby chicken
                for(Entity entity : item.getNearbyEntities(0, 0, 0)) {
                    if(entity instanceof Chicken) {
                        if(normalize(eggTracker, event, entity.getLocation())) {
                            event.setCancelled(true);
                            plugin.debug("Egg spawn cancelled");
                            return;
                        }
                        break;
                    }
                }                
                break;
        }
        
    }
    
}
