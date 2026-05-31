package net.enelson.sopafterworld.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.enelson.sopafterworld.SopAfterworld;

public class EntityDeathEvent implements Listener {
	@EventHandler
    public void entityDead(org.bukkit.event.entity.EntityDeathEvent e) {
		if(!SopAfterworld.afterworld.equals(e.getEntity().getWorld().getName()))
			return;
		e.getDrops().clear();
	}
}

