package vork.server.game;

import lombok.AllArgsConstructor;
import vork.server.game.entity.Direction;

@AllArgsConstructor
public class Location {

public int x, y;
	
	public Location() {
		
	}
	
	public Location(Location l) {
		this.x = l.x;
		this.y = l.y;
	}
	
	public Location add(Location l) {
		return new Location(x + l.x, y + l.y);
	}
	
	public Location add(int x, int y) {
		return new Location(this.x + x, this.y + y);
	}
	
	public Location add(Direction direction) {
		return this.add(fromDirection(direction));
	}
	
	public Location sub(Location l) {
		return new Location(x - l.x, y - l.y);
	}
	
	public Location sub(int x, int y) {
		return new Location(this.x - x, this.y - y);
	}
	
	public Direction toDirection() {
		if (x > 0) return Direction.EAST;
		if (x < 0) return Direction.WEST;
		if (y > 0) return Direction.NORTH;
		if (y < 0) return Direction.SOUTH;
		return Direction.NONE;
	}
	
	public static Location fromDirection(Direction direction) {
		switch (direction) {
		default:
			return new Location(0, 0);
		case NORTH:
			return new Location(0, +1);
		case SOUTH:
			return new Location(0, -1);
		case EAST:
			return new Location(+1, 0);
		case WEST:
			return new Location(-1, 0);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Location)) {
			return false;
		}
		Location ol = (Location) o;
		return ol.x == x && ol.y == y;
	}
	
	@Override
	public String toString() {
		return "{" + x + ", " + y + "}";
	}
}
