package vork.server.game.world;

import lombok.AllArgsConstructor;
import vork.server.game.Location;
import vork.server.game.entity.Entity;

@AllArgsConstructor
public class Area {
	private int minX, minY;
	private int maxX, maxY;
	
	public boolean isInside(Entity entity) {
		return contains(entity.currentLocation);
	}
	
	public boolean contains(Location location) {
		return location.x >= minX && location.x <= maxX &&
			   location.y >= minY && location.y <= maxY;
	}
}
