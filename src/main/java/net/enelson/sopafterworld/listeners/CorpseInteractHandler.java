package net.enelson.sopafterworld.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import net.enelson.sopafterworld.SopAfterworld;
import net.enelson.sopafterworld.corpses.PlayerCorpse;

public class CorpseInteractHandler implements Listener {

	@EventHandler
	public void onNPCInteract(PlayerInteractEntityEvent e) {
		Player player = e.getPlayer();
		PlayerCorpse corpse = SopAfterworld.corpseManager.getCorpse(e.getRightClicked());

		if(corpse == null)
			return;
		
		SopAfterworld.am.createAS(player, corpse);
		player.openInventory(corpse.getInv());
	}
}

