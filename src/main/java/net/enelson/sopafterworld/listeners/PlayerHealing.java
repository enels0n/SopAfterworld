package net.enelson.sopafterworld.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import net.enelson.sopafterworld.SopAfterworld;

public class PlayerHealing implements Listener {
	@EventHandler
	public void playerRegen(EntityRegainHealthEvent e) {
		if((e.getEntity() instanceof Player) && SopAfterworld.afterworld.equals(e.getEntity().getWorld().getName()))
			e.setCancelled(true);
	}
}

