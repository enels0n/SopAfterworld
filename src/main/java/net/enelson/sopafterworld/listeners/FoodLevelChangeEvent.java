package net.enelson.sopafterworld.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.enelson.sopafterworld.SopAfterworld;
import net.enelson.sopafterworld.data.PlayerData;

public class FoodLevelChangeEvent implements Listener {
	@EventHandler
	public void entityDead(org.bukkit.event.entity.FoodLevelChangeEvent e) {
		if (!(e.getEntity() instanceof Player))
			return;

		Player player = (Player) e.getEntity();
		PlayerData pd = SopAfterworld.playerManager.getPlayerData(player);

		if (!pd.isDead())
			return;
		if (!SopAfterworld.afterworld.equals(e.getEntity().getWorld().getName()))
			return;
		
		e.setCancelled(true);
		player.setFoodLevel(20);
	}
}

