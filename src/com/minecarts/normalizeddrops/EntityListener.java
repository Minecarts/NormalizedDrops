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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;


public class EntityListener extends org.bukkit.event.entity.EntityListener {
    
    private final NormalizedDrops plugin;
    
    private final Random random = new Random();
    private final HashMap<World, ArrayList<LocalEvent>> nearbyDeathTracker = new HashMap<World, ArrayList<LocalEvent>>();
    private final HashMap<World, ArrayList<LocalEvent>> nearbyItemSpawnTracker = new HashMap<World, ArrayList<LocalEvent>>();
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

            if(event.elapsed() > plugin.getConfig().getInt("timeFactor") * 1000) {
                // event expired, so clear the list up until this point
                if(itr.previousIndex() >= 0) {
                    events.subList(0, itr.previousIndex()).clear();
                }
                break;
            }
            else {
                // check if event is within radius of point
                if(event.within(point, radius)) {
                    count++;
                }
           }
        }
        
        return count;
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
                Location point = entity.getLocation();
                
                int deathCount = getNearbyCount(nearbyDeathTracker, point, plugin.getConfig().getDouble("radius"));
                int r = random.nextInt(plugin.getConfig().getInt("maxDeaths")) + plugin.getConfig().getInt("minDeaths");
                
                ArrayList<LocalEvent> nearbyDeaths = nearbyDeathTracker.get(point.getWorld());
                if(nearbyDeaths == null) {
                    nearbyDeaths = new ArrayList<LocalEvent>();
                    nearbyDeathTracker.put(point.getWorld(), nearbyDeaths);
                }
                
                // normalization triggered, clear drops
                if(deathCount > r) {
                    event.getDrops().clear();
                    plugin.debug("Normalized {0} drops in {1} @ {2},{3},{4} (NearbyDeaths: {5} > RND: {6}, TrackerSize: {7})", entity, point.getWorld(), point.getX(), point.getY(), point.getZ(), deathCount, r, nearbyDeaths.size());
                    return;
                }
                
                // add local event to tracker
                nearbyDeaths.add(new LocalEvent(event, point));
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
        
    }
    
}
