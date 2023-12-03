package vork.server.game.container;

import java.util.LinkedList;
import java.util.Queue;

import vork.server.Constants;
import vork.server.game.GroundItem;

public class GroundItemsList {
	private final GroundItem[] groundItems = new GroundItem[Constants.MAX_GROUND_ITEMS];
	private final Queue<Integer> available = new LinkedList<>();
	
	public GroundItemsList() {
		for (int i = 0; i < Constants.MAX_GROUND_ITEMS; i++) {
			available.add(i);
		}
	}
	
	public boolean isFull() {
		return available.isEmpty();
	}
	
	public int add(GroundItem groundItem) {
		int index = available.poll();
		groundItems[index] = groundItem;
		return index;
	}
	
	public void remove(int index) {
		groundItems[index] = null;
		available.add(index);
	}
	
	public GroundItem get(int index) {
		return groundItems[index];
	}
}
