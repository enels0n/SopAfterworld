package net.enelson.sopafterworld.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.enelson.sopafterworld.SopAfterworld;
import net.enelson.sopafterworld.data.PlayerData;
import net.enelson.sopafterworld.util.Utils;

public class PlayerJoinEvent implements Listener {
	@EventHandler
	public void onJoin(org.bukkit.event.player.PlayerJoinEvent e) {
		Player p = e.getPlayer();
		PlayerData pd = SopAfterworld.playerManager.addPlayer(p);
		
		if(pd.isDead()) {
			p.sendMessage(Utils.color(SopAfterworld.config.getString("messages.death")));
			return;
		}
		if(pd.getPlayer().getGameMode() == GameMode.SPECTATOR)
			return;
		if(!SopAfterworld.worlds.contains(p.getWorld().getName()))
			return;
		if(!p.getWorld().getBlockAt(p.getLocation().clone().add(0, -1, 0)).getType().isSolid())
			return;
		if(p.getWorld().getBlockAt(p.getLocation()).getType() == Material.LAVA)
			return;
		
		pd.addLastSafeLocation(p.getLocation());
	}
}

