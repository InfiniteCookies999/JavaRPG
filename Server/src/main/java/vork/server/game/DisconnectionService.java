package vork.server.game;

import vork.server.Server;
import vork.server.game.entity.Player;
import vork.server.game.world.Chunk;
import vork.server.net.NetworkHandle;

public class DisconnectionService {
	
	public void onDisconnection(NetworkHandle handle) {
		Player player = handle.getPlayer();
		if (player != null) {
			Server.instance.removePlayer(player);
			Chunk chunk = player.getChunk();
			if (chunk != null) {
				chunk.removeEntity(player);
			}
		}
	}
	
}
