package net.enelson.sopafterworld.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import net.enelson.sopafterworld.SopAfterworld;
import net.enelson.sopafterworld.corpses.PlayerCorpse;

public class CorpseInteractHandler implements Listener {

	@EventHandler
	public void onNPCInteract(PlayerInteractEntityEvent e) {
		openCorpseInventory(e.getPlayer(), SopAfterworld.corpseManager.getCorpse(e.getRightClicked()), e);
	}

	@EventHandler
	public void onNPCInteractAt(PlayerInteractAtEntityEvent e) {
		openCorpseInventory(e.getPlayer(), SopAfterworld.corpseManager.getCorpse(e.getRightClicked()), e);
	}

	@EventHandler
	public void onCorpseHit(EntityDamageByEntityEvent e) {
		if (!(e.getDamager() instanceof Player)) {
			return;
		}
		openCorpseInventory((Player) e.getDamager(), SopAfterworld.corpseManager.getCorpse(e.getEntity()), e);
	}

	public static void openCorpseInventory(Player player, PlayerCorpse corpse, Event event) {
		if(corpse == null)
			return;

		if (event instanceof org.bukkit.event.Cancellable) {
			((org.bukkit.event.Cancellable) event).setCancelled(true);
		}
		SopAfterworld.am.createAS(player, corpse);
		player.openInventory(corpse.getInv());
	}
}

