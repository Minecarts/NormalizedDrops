package com.minecarts.normalizeddrops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.ListIterator;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Item;
import org.bukkit.entity.Chicken;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;

import static org.bukkit.Material.*;


public class EntityListener extends org.bukkit.event.entity.EntityListener {
    
    private final NormalizedDrops plugin;
    
    private final Random random = new Random();
    private final HashMap<World, ArrayList<LocalEvent>> nearbyDeathTracker = new HashMap<World, ArrayList<LocalEvent>>();
    private final HashMap<World, ArrayList<LocalEvent>> nearbyEggTracker = new HashMap<World, ArrayList<LocalEvent>>();
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
    
    
    
    @Override
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.isCancelled()) return;
        
        ArrayList<EntityDamageEvent> history = damageTracker.get(event.getEntity());
        
        if(history == null) {
            history = new ArrayList<EntityDamageEvent>();
            damageTracker.put(event.getEntity(), history);
        }
        
        history.add(event);
    }
    
    @Override
    public void onEntityDeath(EntityDeathEvent event) {
        final Entity entity = event.getEntity();
        
        // skip normalization for player drops
        if(entity instanceof Player) return;
        
        // is this check necessary?
        // we're in the death event, so it's assumed that the entity was living
        if(entity instanceof LivingEntity) {
            // normalize items dropped
            if(event.getDrops().size() > 0) {
                if(normalize(nearbyDeathTracker, event, entity.getLocation())) {
                    plugin.debug("Clearing drops for {0}: ", entity, event.getDrops());
                    event.getDrops().clear();
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
                event.setDroppedExp(totalDamage > 0 && playerDamage > 0 ? Math.round(exp * (playerDamage / totalDamage)) : 0);
            }
        }
    }
    
    
    @Override
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = (Item) event.getEntity();
        
        switch(item.getItemStack().getType()) {
            case EGG:
                // since ITEM_SPAWN has no causes, check if egg was laid by a nearby chicken
                for(Entity entity : item.getNearbyEntities(0, 0, 0)) {
                    if(entity instanceof Chicken) {
                        if(normalize(nearbyEggTracker, event, entity.getLocation())) {
                            plugin.debug("Cancelling egg spawn");
                            event.setCancelled(true);
                        }
                        break;
                    }
                }                
                break;
        }
        
    }
    
}
