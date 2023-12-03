package vork.server.game.world;

import java.util.ArrayList;
import java.util.List;

import vork.server.Constants;
import vork.server.game.entity.Entity;
import vork.server.game.entity.Npc;
import vork.server.game.entity.Player;

public class Chunk {
	public int chunkX, chunkY;
	
	public boolean[] collision = new boolean[Constants.CHUNK_SIZE*Constants.CHUNK_SIZE];
	
	public List<Player> players = new ArrayList<>();
	public List<Npc> npcs = new ArrayList<>();
	
	public Chunk(int chunkX, int chunkY) {
		this.chunkX = chunkX;
		this.chunkY = chunkY;
	}
	
	public void addEntity(Entity entity) {
		entity.setChunk(this);
		if (entity instanceof Player) {
			players.add((Player) entity);
		} else {
			npcs.add((Npc) entity);
		}
	}
	
	public void removeEntity(Entity entity) {
		if (entity instanceof Player) {
			players.remove(entity);
		} else {
			npcs.remove(entity);
		}
	}
}
