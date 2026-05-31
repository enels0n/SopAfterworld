package net.enelson.sopafterworld.corpses;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SitTrait;
import net.citizensnpcs.trait.SkinTrait;
import net.enelson.sopafterworld.SopAfterworld;
import net.enelson.sopafterworld.util.Utils;

public class PlayerCorpse {
	private Long createTime;
	private NPC npc;
	private Inventory inv;
	private String playerName;
	private String uuid;
	private boolean saved;
	private Location location;
	
	PlayerCorpse(Player player) {
		this.saved = true;
		this.uuid = player.getUniqueId().toString();
		this.createTime = System.currentTimeMillis() / 1000;
		
		this.playerName = player.getName();
		this.inv = Bukkit.createInventory(null, 45, Utils.color("&7Труп &f" + player.getName()));
		this.inv.setContents(player.getInventory().getContents());
		
		this.location = SopAfterworld.playerManager.getPlayerData(player).getLastSafeLocation();

		this.createNPC();
		
		this.save();
	}
	
	PlayerCorpse(String uuid, Long createTime, ItemStack[] inventory, String playerName, Location location) {
		this.uuid = uuid;
		this.createTime = createTime;
		this.playerName = playerName;
		this.location = location;
		
		this.inv = Bukkit.createInventory(null, 45, Utils.color("&7Труп &f" + this.playerName));
		this.inv.setContents(inventory);
		
		this.createNPC();
	}
	
	public Long getCreateTime() {
		return this.createTime;
	}
	
	public String getPlayerName() {
		return this.playerName;
	}
	
	public NPC getNPC() {
		return this.npc;
	}
	
	public boolean isSaved() {
		return this.saved;
	}
	
	public void setNotSafe() {
		this.saved = false;
	}
	
	public Inventory getInv() {
		return this.inv;
	}
	
	public void removeCorpse() {
		this.npc.destroy();
		
		this.closeInventories();

		String filePath = SopAfterworld.plugin.getDataFolder().getAbsolutePath() + File.separator + "corpses/"
				+ this.uuid + ".yml";
		File file = new File(filePath);
		if (file.exists()) {
			file.delete();
		}
	}
	
	public void dropItems() {
		for (ItemStack item : this.inv.getContents()) {
			if (item != null) {
				this.npc.getStoredLocation().getWorld().dropItem(this.npc.getStoredLocation(), item.clone());
			}
		}
		this.inv.clear();
	}

	public void returnItems(Player player) {
		this.closeInventories();
		
		ItemStack[] inventory = this.inv.getContents();
		ItemStack helmet = null;
		ItemStack chestplate = null;
		ItemStack leggings = null;
		ItemStack boots = null;
		ItemStack offHand = null;
		
		if (inventory[39] != null) {
			helmet = inventory[39].clone();
			inventory[39] = null;
		}
		if (inventory[38] != null) {
			chestplate = inventory[38].clone();
			inventory[38] = null;
		}
		if (inventory[37] != null) {
			leggings = inventory[37].clone();
			inventory[37] = null;
		}
		if (inventory[36] != null) {
			boots = inventory[36].clone();
			inventory[36] = null;
		}
		if (inventory[40] != null) {
			offHand = inventory[40].clone();
			inventory[40] = null;
		}
		for (int y = 41; y < 45;) {
			if (inventory[y] != null) {
				player.getWorld().dropItem(player.getLocation(), inventory[y].clone());
			}
			inventory[y++] = null;
		}
		
		inventory = Arrays.stream(inventory)
                .filter(s -> (s != null))
                .toArray(ItemStack[]::new);

		player.getInventory().addItem(inventory);
		player.getInventory().setHelmet(helmet);
		player.getInventory().setChestplate(chestplate);
		player.getInventory().setLeggings(leggings);
		player.getInventory().setBoots(boots);
		player.getInventory().setItemInOffHand(offHand);
	}
	
	public void save() {
		String filePath = SopAfterworld.plugin.getDataFolder().getAbsolutePath() + File.separator + "corpses/"
				+ this.uuid + ".yml";
		File file = new File(filePath);
		
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
			}
		}

		FileConfiguration config = new YamlConfiguration();
		if (this.npc == null) {
			for (ItemStack item : inv.getContents()) {
				location.getWorld().dropItem(location, item);
			}
			config.set("", null);
			SopAfterworld.corpseManager.removeCorpse(this.npc);
		} else {
			if (this.npc.getEntity() == null) {
				this.npc.spawn(location);
			}
			config.set("createTime", createTime);
			config.set("playerName", playerName);
			config.set("location", location);
	
			int i = 0;
			for (ItemStack item : inv.getContents()) {
				config.set("inventory." + i, item);
				i++;
			}
		}
		try {
			config.save(file);
		} catch (IOException e) {
		}
		this.saved = true;
	}
	
	public void closeInventories() {
		for (int i = this.inv.getViewers().size() - 1; i >= 0; --i) {
			Player p = (Player) this.inv.getViewers().get(i);
			p.closeInventory();
			SopAfterworld.am.removeAS(p);
		}
	}
	
	private void createNPC() {
		this.npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, this.playerName, this.location);
		String corpseName = Utils.color("&8Труп &f" + this.playerName);
		this.npc.setName(corpseName);
		
		Bukkit.getScheduler().runTaskLater(SopAfterworld.plugin, () -> {
			this.npc.getOrAddTrait(SkinTrait.class).setSkinName(playerName);
			this.npc.getOrAddTrait(SitTrait.class).setSitting(location);
		}, 20);
	}
}
