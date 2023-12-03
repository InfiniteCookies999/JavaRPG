package client.net.in;

import java.io.IOException;

import client.Client;
import client.Constants;
import client.game.PlayScreen;
import client.game.entity.Entity;
import client.game.world.Location;
import client.net.NetOpcodes;
import client.net.NetworkHandle;
import client.net.in.EntityTeleportListener.EntityTeleportPacket;
import lombok.AllArgsConstructor;

public class EntityTeleportListener extends PacketListener<EntityTeleportPacket> {

	@AllArgsConstructor
	class EntityTeleportPacket extends PacketData {
		private int entityId;
		private int tileX, tileY;
	}
	
	public EntityTeleportListener() {
		super(NetOpcodes.ENTITY_TELEPORT);
	}

	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		int entityId = handle.readUnsignedInt24();
		int tileX = handle.readInt();
		int tileY = handle.readInt();
		
		return new EntityTeleportPacket(entityId, tileX, tileY);
	}

	@Override
	public void onEvent(EntityTeleportPacket packet) {
		Entity entity = Client.instance.getEntity(packet.entityId);
		if (entity == null) {
			return;
		}
		
		Location teleportLocation = new Location(packet.tileX, packet.tileY);
		
		PlayScreen playScreen = Client.instance.getScreen();
		playScreen.handleEntityTeleported(entity);
		
		entity.moveQueue.clear();
		entity.x = packet.tileX * Constants.TILE_SIZE;
		entity.y = packet.tileY * Constants.TILE_SIZE;
		entity.currentLocation = new Location(teleportLocation);
		entity.futureLocation = new Location(teleportLocation);
		
	}
}
