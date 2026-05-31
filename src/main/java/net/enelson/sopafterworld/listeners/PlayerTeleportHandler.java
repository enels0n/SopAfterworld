package net.enelson.sopafterworld.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.enelson.sopafterworld.SopAfterworld;
import net.enelson.sopafterworld.data.PlayerData;

public class PlayerTeleportHandler implements Listener {
	@EventHandler
	public void checkTeleport(org.bukkit.event.player.PlayerTeleportEvent e) {
		PlayerData pd = SopAfterworld.playerManager.getPlayerData(e.getPlayer());
		
		if(pd == null)
			return;
		
		if (pd.isDead() && !SopAfterworld.afterworld.equals(e.getTo().getWorld().getName())) {
			e.setCancelled(true);
			return;
		}

		if (!pd.isDead() && SopAfterworld.afterworld.equals(e.getTo().getWorld().getName())
				&& !(e.getPlayer().hasPermission("magesreincarnation.bypass") || e.getPlayer().isOp())) {
			e.setCancelled(true);
			return;
		}
	}
}

