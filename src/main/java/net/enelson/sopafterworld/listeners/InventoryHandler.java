package net.enelson.sopafterworld.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import net.enelson.sopafterworld.SopAfterworld;
import net.enelson.sopafterworld.corpses.PlayerCorpse;
import net.enelson.sopafterworld.util.Utils;

public class InventoryHandler implements Listener {

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		PlayerCorpse corpse = SopAfterworld.corpseManager.getCorpse(e.getInventory());
		if (corpse != null) {
			if (Utils.checkNotDropping(e.getClickedInventory().getItem(e.getSlot()))) {
				e.setCancelled(true);
				return;
			}
			corpse.setNotSafe();
		} else {
			corpse = SopAfterworld.corpseManager.getCorpse(e.getClickedInventory());
			if (corpse != null) {
				if (Utils.checkNotDropping(e.getClickedInventory().getItem(e.getSlot()))) {
					e.setCancelled(true);
					return;
				}
				corpse.setNotSafe();
			}
		}
	}

	@EventHandler
	public void onMove(InventoryDragEvent e) {
		PlayerCorpse corpse = SopAfterworld.corpseManager.getCorpse(e.getInventory());
		if (corpse != null) {
			corpse.setNotSafe();
			return;
		}
	}
}

