package net.enelson.sopafterworld.listeners;

import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import net.enelson.sopafterworld.SopAfterworld;
import net.enelson.sopafterworld.data.PlayerData;
import net.enelson.sopafterworld.util.Utils;

public class PlayerDeathEvent implements Listener {
	@EventHandler(priority = EventPriority.MONITOR)
	public void setPlayerDead(org.bukkit.event.entity.PlayerDeathEvent e) {
		Player player = (Player) e.getEntity();
		
		if(!SopAfterworld.worlds.contains(player.getWorld().getName())) {
			for(ItemStack item : e.getDrops()) {
				if (Utils.checkNotDropping(item)) {
					item.setAmount(0);
					return;
				}
			}
			return;
		}
		if(!Utils.hasPermit(player))
			return;
		
		PlayerData pd = SopAfterworld.playerManager.getPlayerData(player);
		Location deathLocation = e.getEntity().getLocation().clone();
		pd.addDeath();
		pd.setPortal(Utils.searchPortalPoint());
		pd.setDead(true);
		SopAfterworld.corpseManager.createCorpse(player, e.getDrops(), deathLocation);
		e.getDrops().clear();
	}
}

