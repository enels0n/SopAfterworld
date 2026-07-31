package net.enelson.sopafterworld.command;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.enelson.sopafterworld.SopAfterworld;
import net.enelson.sopafterworld.data.PlayerData;
import net.enelson.sopafterworld.util.Utils;


public class PortalCommand implements CommandExecutor{
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player))
			return true;
		
		PlayerData pd = SopAfterworld.playerManager.getPlayerData((Player)sender);
		if (pd == null || !pd.isDead())
			return true;

		pd.setPortal(Utils.searchPortalPoint());

		Location loc = pd.getAfterworldLocation();
		if (loc != null) {
			Block block = loc.getBlock();
			if (block.getType() != Material.LAVA && block.getType() != Material.MAGMA_BLOCK && block.getType() != Material.FIRE) {
				pd.getPlayer().teleport(loc);
			}
		}
		return true;
	}
}

