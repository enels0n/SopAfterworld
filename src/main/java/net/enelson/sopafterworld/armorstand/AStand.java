package net.enelson.sopafterworld.armorstand;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import net.enelson.sopafterworld.corpses.PlayerCorpse;
import net.enelson.sopafterworld.util.Utils;

public class AStand {
	private Player player;
	private ArmorStand stand;
	private Long createTime;
	private PlayerCorpse corpse;
	
	AStand(Player player, PlayerCorpse corpse) {
		this.player = player;
		this.corpse = corpse;
		
		this.stand = (ArmorStand) player.getWorld().spawn(player.getLocation().clone().add(0,0.1,0), ArmorStand.class);
		this.stand.setCustomName(Utils.color("*&eРћСЃРјР°С‚СЂРёРІР°РµС‚ С‚СЂСѓРї " + corpse.getPlayerName() + "&f*"));
		this.stand.setCustomNameVisible(true);
		this.stand.setGravity(false);
		this.stand.setVisible(false);
		this.stand.setInvisible(true);
		
		this.createTime = System.currentTimeMillis()/1000;
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	public ArmorStand getStand() {
		return this.stand;
	}
	
	public Long getCreateTime() {
		return this.createTime;
	}
	
	public PlayerCorpse getCorpse() {
		return this.corpse;
	}
	
	public void destroy() {
		this.stand.remove();
	}
	
	public void updateLocation() {
		this.stand.teleport(this.player.getLocation().clone().add(0,0.1,0));
	}
}

