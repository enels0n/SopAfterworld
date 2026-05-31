package net.enelson.sopafterworld.corpses;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import net.citizensnpcs.api.npc.NPC;
import net.enelson.sopafterworld.SopAfterworld;

public class CorpseManager {
	private List<PlayerCorpse> corpses;
	private BukkitTask taskRemover;
	
	public CorpseManager() {
		this.corpses = new ArrayList<PlayerCorpse>();

		for (File file : new File(SopAfterworld.plugin.getDataFolder().getAbsolutePath() + File.separator + "corpses/").listFiles()) {
			if(!file.getName().endsWith(".yml"))
				continue;
			
			String name = file.getName();
			String uuid = name.replaceAll("(?<!^)[.]" + "[^.]*$", "");
			
			FileConfiguration config = YamlConfiguration.loadConfiguration(file);
			Long createTime = config.getLong("createTime");
			String playerName = config.getString("playerName");
			Location location = config.getLocation("location");
			ItemStack[] inventory = new ItemStack[45];
			
			for(int i = 0; i < inventory.length; i++) {
				inventory[i] = config.getItemStack("inventory."+i);
			}
			
			this.corpses.add(new PlayerCorpse(uuid, createTime, inventory, playerName, location));
	    }
		
		this.taskRemover = Bukkit.getScheduler().runTaskTimer(SopAfterworld.plugin, () -> {
			for(Iterator<PlayerCorpse> it = this.corpses.iterator(); it.hasNext();) {
				PlayerCorpse corpse = it.next();
				if(corpse.getCreateTime()+SopAfterworld.config.getInt("decrease-time") <= System.currentTimeMillis()/1000) {
					corpse.dropItems();
					corpse.removeCorpse();
					it.remove();
				}
				else if(!corpse.isSaved()){
					corpse.save();
				}
			}
		}, 5*20, 5*20);
	}
	
	public void createCorpse(Player player) {
		if(!player.getInventory().isEmpty()) {
			PlayerCorpse corpse = new PlayerCorpse(player);
			this.corpses.add(corpse);
		}
	}
	
	public PlayerCorpse getCorpse(Player player) {
		return this.corpses.stream().filter(c -> c.getPlayerName().equals(player.getName())).findFirst().orElse(null);
	}
	
	public PlayerCorpse getCorpse(Entity entity) {
		return this.corpses.stream().filter(c -> c.getNPC() != null && c.getNPC().getEntity() != null && c.getNPC().getEntity().equals(entity)).findFirst().orElse(null);
	}
	
	public PlayerCorpse getCorpse(NPC npc) {
		return this.corpses.stream().filter(c -> c.getNPC().equals(npc)).findFirst().orElse(null);
	}
	
	public PlayerCorpse getCorpse(Inventory inv) {
		return this.corpses.stream().filter(c -> c.getInv().equals(inv)).findFirst().orElse(null);
	}
	
	public void removeCorpse(Player player) {
		PlayerCorpse playerCorpse = this.getCorpse(player);
		if(playerCorpse != null) {
			playerCorpse.removeCorpse();
			this.corpses.remove(playerCorpse);
		}
	}
	
	public void removeCorpse(NPC npc) {
		PlayerCorpse playerCorpse = this.getCorpse(npc);
		if(playerCorpse != null) {
			playerCorpse.removeCorpse();
			this.corpses.remove(playerCorpse);
		}
	}
	
	public void deInit() {
		this.corpses.forEach(c -> { c.save(); c.closeInventories(); c.getNPC().destroy(); });
		this.taskRemover.cancel();
	}
}

