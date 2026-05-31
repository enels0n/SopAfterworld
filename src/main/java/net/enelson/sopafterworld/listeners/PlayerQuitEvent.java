package net.enelson.sopafterworld.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.enelson.sopafterworld.SopAfterworld;

public class PlayerQuitEvent implements Listener {
	@EventHandler
	public void onQuit(org.bukkit.event.player.PlayerQuitEvent e) {
		SopAfterworld.playerManager.removePlayer(e.getPlayer());
		Player player = (Player)e.getPlayer();
		SopAfterworld.am.removeAS(player);
	}
}

