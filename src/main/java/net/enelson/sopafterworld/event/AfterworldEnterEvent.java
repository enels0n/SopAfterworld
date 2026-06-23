package net.enelson.sopafterworld.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called once when a player enters the afterworld (the other world).
 */
public class AfterworldEnterEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;

	public AfterworldEnterEvent(Player player) {
		this.player = player;
	}

	public Player getPlayer() {
		return this.player;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
