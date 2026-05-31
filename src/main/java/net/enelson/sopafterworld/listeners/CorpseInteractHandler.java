package net.enelson.sopafterworld.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.enelson.sopafterworld.SopAfterworld;
import net.enelson.sopafterworld.corpses.PlayerCorpse;

public class CorpseInteractHandler implements Listener {

	/**
	@EventHandler
	public void onNPCInteract(NPC.Events.Interact event) {
		Player player = event.getPlayer();
		NPC npc = event.getNPC();

		PlayerCorpse corpse = SopAfterworld.corpseManager.getCorpse(npc);
		SopAfterworld.am.createAS(player, corpse);
		player.openInventory(corpse.getInv());
	}**/

	@EventHandler
	public void onNPCInteract(PlayerInteractEntityEvent e) {
		Player player = e.getPlayer();
		PlayerCorpse corpse = SopAfterworld.corpseManager.getCorpse(e.getRightClicked());

		if(corpse == null)
			return;
		
		SopAfterworld.am.createAS(player, corpse);
		player.openInventory(corpse.getInv());
	}

	@EventHandler
	public void onNPCInteract(NPCRightClickEvent e) {
		Player player = e.getClicker();
		PlayerCorpse corpse = SopAfterworld.corpseManager.getCorpse(e.getNPC());

		if(corpse == null)
			return;
		
		SopAfterworld.am.createAS(player, corpse);
		player.openInventory(corpse.getInv());
	}
}

