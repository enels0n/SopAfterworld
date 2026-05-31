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
		if(!(sender instanceof Player))
			return false;
		
		PlayerData pd = SopAfterworld.playerManager.getPlayerData((Player)sender);
		if(pd == null || !pd.isDead())
			return false;

		pd.setPortal(Utils.searchPortalPoint());
		
		int i = 0;
		Location loc;
		while(i<1) {
			loc = pd.getAfterworldLocation();
			Block block = loc.getBlock();
			if (block.getType() != Material.LAVA && block.getType() != Material.MAGMA_BLOCK && block.getType() != Material.FIRE) {
				pd.getPlayer().teleport(loc.add(0,1,0));
				break;
			}
		}
		return false;
	}
}

