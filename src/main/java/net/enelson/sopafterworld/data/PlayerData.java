package net.enelson.sopafterworld.data;

import java.io.File;
import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import net.enelson.sopafterworld.SopAfterworld;
import net.enelson.sopafterworld.util.Serializer;
import net.enelson.sopafterworld.util.Utils;

public class PlayerData {

	private Player player;
	private String uuid;
	private Boolean dead;
	private Boolean guideCloseMessageSended;
	private Location portal[];
	private LastLocationList lastSafeLocations = new LastLocationList();
	private int deaths;
	private Long lastDecrease;
	private Long lastOnline;
	private Long lastRebirth;
	private BukkitTask br;
	private File file;
	private FileConfiguration playerConfig;
	
	PlayerData(Player player) {
		this.player = player;
		this.uuid = player.getUniqueId().toString().toLowerCase();
		String filePath = SopAfterworld.plugin.getDataFolder().getAbsolutePath() + File.separator + "players/"
				+ this.uuid + ".yml";
		this.file = new File(filePath);
		
		if (!this.file.exists()) {
			try {
				this.file.createNewFile();
				FileConfiguration cfg = new YamlConfiguration();
				cfg.set("name", player.getName());
				cfg.set("first-join", System.currentTimeMillis() / 1000);
				cfg.set("death-count", 0);
				cfg.set("last-decrease", 0);
				cfg.set("lastOnline", 0);
				cfg.set("isDead", false);
				
				cfg.save(this.file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		this.playerConfig = YamlConfiguration.loadConfiguration(this.file);
		if (this.playerConfig.getList("lastSafeLocations") != null) {
	        for (Object l : this.playerConfig.getList("lastSafeLocations")) {
	        	lastSafeLocations.add((Location) l);
	        }
		}
		this.dead = this.playerConfig.getBoolean("isDead");
		this.deaths = this.playerConfig.getInt("death-count");
		this.lastDecrease = this.playerConfig.getLong("last-decrease");
		this.lastOnline = 0L;
		this.portal = Serializer.getDeserializedLocation(this.playerConfig.getStringList("portal"));
		this.guideCloseMessageSended = false;
		this.lastRebirth = 0L;
	}
	
	public Location[] getPortalLocations() {
		return this.portal;
	}
	
	public Location getPortalLocation() {
		return this.portal[0];
	}

	public String getUuid() {
		return this.uuid;
	}
	
	public String getName() {
		return this.player.getDisplayName();
	}
	
	public Player getPlayer() {
		return this.player;
	}
	
	public Location getLastSafeLocation() {
		for (Location l : this.lastSafeLocations.get()) {
	    	if (l == null) {
	    		continue;
	    	}
			if (!l.clone().add(0, -1, 0).getBlock().getType().isSolid()) {
				continue;
			}
			if (l.getWorld().getBlockAt(l).getType() == Material.LAVA) {
				continue;
			}
			
			return l;
	    }
		return Serializer.getDeserializedLocation(SopAfterworld.config.getString("default-reincarnation-point"));
	}
	
	public Boolean isDead() {
		return this.dead;
	}
	
	public int getDeaths() {
		return this.deaths;
	}
	
	public Long getLastDecrease() {
		return this.lastDecrease;
	}
	
	public Long getLastRebirth() {
		return this.lastRebirth;
	}
	
	public Location getAfterworldLocation() {
		return Utils.findAfterworldSpawnLocation(this.portal[0], Utils.getRadius(this.deaths));
	}
	
	public Long getLastOnline() {
		return this.lastOnline;
	}
	
	public Boolean isGuideCloseMessageSended() {
		return this.guideCloseMessageSended;
	}
	
	public void setLastDecrease(Long lastDecrease) {
		this.lastDecrease = lastDecrease;
		this.saveConfigs();
	}
	
	public void setLastRebirth(Long lastRecrease) {
		this.lastRebirth = lastRecrease;
	}
	
	public void addDeath() {
		this.deaths += 1;
		this.lastDecrease = System.currentTimeMillis() / 1000;
		this.saveConfigs();
	}
	
	public void removeDeath() {
		this.deaths -= 1;
		this.lastDecrease = System.currentTimeMillis() / 1000;
		this.saveConfigs();
	}

	public void setDeaths(int deaths) {
		this.deaths = Math.max(0, deaths);
		this.lastDecrease = System.currentTimeMillis() / 1000;
		this.saveConfigs();
	}
	
	public void setDead(Boolean dead) {
		this.dead = dead;
		this.guideCloseMessageSended = false;
		if (this.br != null) {
			this.br.cancel();
		}
		this.saveConfigs();
	}
	
	public void setGuideCloseMessageSended(Boolean sended) {
		this.guideCloseMessageSended = sended;
	}
	
	public void setGuideClose() {
		this.player.sendMessage(Utils.color(SopAfterworld.config.getString("messages.too-close")));
		this.guideCloseMessageSended = true;
		final PlayerData pd = this;
		br = new BukkitRunnable() {
			@Override
			public void run() {
				pd.setGuideCloseMessageSended(false);
			}
		}.runTaskLater(SopAfterworld.plugin, 1200L);
	}
	
	public void setPortal(Location location) {
		this.portal = Utils.createPortalLocations(location);
		this.saveConfigs();
	}
	
	public void addLastSafeLocation(Location location) {
		this.lastSafeLocations.add(location);
	}
	
	public void setLastOnline() {
		this.lastOnline = System.currentTimeMillis() / 1000;
		this.saveConfigs();
	}
	
	private void saveConfigs() {
		playerConfig.set("death-count", this.deaths);
		playerConfig.set("last-decrease", this.lastDecrease);
		playerConfig.set("lastOnline", this.lastOnline);
		playerConfig.set("lastSafeLocations", this.lastSafeLocations.getBack());
		playerConfig.set("isDead", this.dead);
		playerConfig.set("portal", Serializer.getSerializedLocation(this.portal));
		
		try {
			this.playerConfig.save(this.file);
		} catch (IOException e) {
		}
	}
}
