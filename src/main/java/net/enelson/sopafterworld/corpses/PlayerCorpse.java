package net.enelson.sopafterworld.corpses;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.enelson.sopafterworld.SopAfterworld;
import net.enelson.sopafterworld.util.Utils;
import net.enelson.sopli.lib.SopLib;
import net.enelson.sopli.lib.corpse.CorpseHandle;

public class PlayerCorpse {
    private final Long createTime;
    private CorpseHandle corpseHandle;
    private final Inventory inv;
    private final String playerName;
    private final String uuid;
    private boolean saved;
    private final Location location;

    PlayerCorpse(Player player) {
        this.saved = true;
        this.uuid = player.getUniqueId().toString();
        this.createTime = System.currentTimeMillis() / 1000;
        this.playerName = player.getName();
        this.inv = Bukkit.createInventory(null, 45, Utils.getCorpseInventoryTitle(player));
        this.inv.setContents(player.getInventory().getContents());
        this.location = player.getLocation().clone();

        this.createCorpseEntity();
        this.save();
    }

    PlayerCorpse(Player player, List<ItemStack> drops) {
        this(player, drops, player != null ? player.getLocation().clone() : null);
    }

    PlayerCorpse(Player player, List<ItemStack> drops, Location deathLocation) {
        this.saved = true;
        this.uuid = player.getUniqueId().toString();
        this.createTime = System.currentTimeMillis() / 1000;
        this.playerName = player.getName();
        this.inv = Bukkit.createInventory(null, 45, Utils.getCorpseInventoryTitle(player));
        for (int i = 0; i < Math.min(45, drops.size()); i++) {
            ItemStack item = drops.get(i);
            if (item != null) {
                this.inv.setItem(i, item.clone());
            }
        }

        this.location = deathLocation != null ? deathLocation.clone() : player.getLocation().clone();
        this.createCorpseEntity();
        this.save();
    }

    PlayerCorpse(String uuid, Long createTime, ItemStack[] inventory, String playerName, Location location) {
        this.uuid = uuid;
        this.createTime = createTime;
        this.playerName = playerName;
        this.location = location;
        this.inv = Bukkit.createInventory(null, 45, Utils.getCorpseInventoryTitle(this.playerName));
        this.inv.setContents(inventory);

        this.createCorpseEntity();
    }

    public Long getCreateTime() {
        return this.createTime;
    }

    public String getPlayerName() {
        return this.playerName;
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

    public UUID getCorpseEntityUuid() {
        return corpseHandle != null ? corpseHandle.getEntityUuid() : null;
    }

    public UUID getInteractionEntityUuid() {
        return corpseHandle != null ? corpseHandle.getInteractionEntityUuid() : null;
    }

    public int getVisualEntityId() {
        return corpseHandle != null ? corpseHandle.getVisualEntityId() : -1;
    }

    public void removeCorpse() {
        if (this.corpseHandle != null) {
            this.corpseHandle.remove();
        }

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
            if (item != null && this.location.getWorld() != null) {
                this.location.getWorld().dropItem(this.location, item.clone());
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
                .filter(s -> s != null)
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
            } catch (IOException ignored) {
            }
        }

        FileConfiguration config = new YamlConfiguration();
        if (this.corpseHandle == null || !this.corpseHandle.isValid()) {
            this.createCorpseEntity();
        }
        config.set("createTime", createTime);
        config.set("playerName", playerName);
        config.set("location", location);

        int i = 0;
        for (ItemStack item : inv.getContents()) {
            config.set("inventory." + i, item);
            i++;
        }
        try {
            config.save(file);
        } catch (IOException ignored) {
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

    public boolean matchesEntity(Entity entity) {
        UUID entityUuid = getInteractionEntityUuid();
        return entity != null && entityUuid != null && entityUuid.equals(entity.getUniqueId());
    }

    private void createCorpseEntity() {
        if (SopLib.getInstance() == null || SopLib.getInstance().getCorpseService() == null) {
            this.corpseHandle = null;
            return;
        }
        String corpseName = Utils.getCorpseDisplayName(this.playerName);
        this.corpseHandle = SopLib.getInstance().getCorpseService()
                .createCorpse(this.location, corpseName, this.playerName);
    }
}
