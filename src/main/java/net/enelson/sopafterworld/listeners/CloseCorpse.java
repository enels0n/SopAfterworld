package net.enelson.sopafterworld.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import net.enelson.sopafterworld.SopAfterworld;

public class CloseCorpse implements Listener {
	@EventHandler
	public void closeCorpse(InventoryCloseEvent e) {
		if(!(e.getPlayer() instanceof Player))
			return;
		SopAfterworld.am.removeAS((Player)e.getPlayer());
	}
}

