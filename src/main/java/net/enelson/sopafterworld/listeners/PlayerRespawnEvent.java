package net.enelson.sopafterworld.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import net.enelson.sopafterworld.SopAfterworld;
import net.enelson.sopafterworld.data.PlayerData;
import net.enelson.sopafterworld.event.AfterworldEnterEvent;
import net.enelson.sopafterworld.util.Utils;

public class PlayerRespawnEvent implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	public void toAfterworld(org.bukkit.event.player.PlayerRespawnEvent e) {
		Player player = e.getPlayer();
		PlayerData pd = SopAfterworld.playerManager.getPlayerData(player);
		
		if(!pd.isDead())
			return;

		player.sendMessage(Utils.color(SopAfterworld.config.getString("messages.death")));
		
		int i = 0;
		Location loc;
		while(i<1) {
			loc = pd.getAfterworldLocation();
			Block block = loc.getBlock();
			if (block.getType() != Material.LAVA && block.getType() != Material.MAGMA_BLOCK && block.getType() != Material.FIRE) {
				e.setRespawnLocation(loc);
				Bukkit.getPluginManager().callEvent(new AfterworldEnterEvent(player));
				break;
			}
		}
		
		SopAfterworld.plugin.getServer().getScheduler().runTaskLater(SopAfterworld.plugin, new Runnable() {
			public void run() {
				player.setHealth(2);
			}
		}, 5L);
	}
}

