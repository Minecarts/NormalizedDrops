package com.minecarts.normalizeddrops;

import org.bukkit.Location;
import org.bukkit.event.Event;
import java.util.Date;


public class LocalEvent {
    public final Location location;
    public final Event event;
    public final Date time;

    public LocalEvent(Event event, Location location) {
        this.location = location;
        this.event = event;
        this.time = new Date();
    }

    public boolean within(Location where, double radius) {
        if(Math.abs(location.getX() - where.getX()) > radius) return false;
        if(Math.abs(location.getY() - where.getY()) > radius) return false;
        if(Math.abs(location.getZ() - where.getZ()) > radius) return false;
        return true;
    }
    
    public long elapsed() {
        return elapsed(new Date());
    }
    public long elapsed(Date when) {
        return when.getTime() - time.getTime();
    }
}
