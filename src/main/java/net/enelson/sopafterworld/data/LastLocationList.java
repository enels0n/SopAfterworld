package net.enelson.sopafterworld.data;

import org.bukkit.Location;

public class LastLocationList {
	private Location[] locations;
	private int locationsCount = 5;
	
	public LastLocationList() {
		locations = new Location[5];
	}
	
	public void add(Location location) {
		for(int i = this.locationsCount-1; i>0;) {
			this.locations[i--] = this.locations[i];
		}
		this.locations[0] = location;
	}
	
	public Location[] get() {
		return this.locations;
	}
	
	public Location[] getBack() {
		Location[] locationsBack = new Location[locationsCount];
		int o = 0;
		for(int i = locationsCount; i!=0;) {
			locationsBack[o] = this.locations[--i];
		}
		return locationsBack;
	}
}

