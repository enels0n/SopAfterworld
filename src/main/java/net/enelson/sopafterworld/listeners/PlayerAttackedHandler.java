package net.enelson.sopafterworld.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import net.enelson.sopafterworld.SopAfterworld;
import net.enelson.sopafterworld.data.PlayerData;

public class PlayerAttackedHandler implements Listener {

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof Player))
			return;
		
		PlayerData pd = SopAfterworld.playerManager.getPlayerData((Player)e.getEntity());
		if(pd != null && pd.getLastRebirth()+5 > System.currentTimeMillis()/1000)
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player))
			return;
		
		PlayerData pd = SopAfterworld.playerManager.getPlayerData((Player)e.getEntity());
		if(pd != null && pd.getLastRebirth()+5 > System.currentTimeMillis()/1000)
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onDamage(EntityDamageByBlockEvent e) {
		if (!(e.getEntity() instanceof Player))
			return;
		
		PlayerData pd = SopAfterworld.playerManager.getPlayerData((Player)e.getEntity());
		if(pd != null && pd.getLastRebirth()+5 > System.currentTimeMillis()/1000)
			e.setCancelled(true);
	}
}

