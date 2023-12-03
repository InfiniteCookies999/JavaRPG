package client.net.in;

import java.io.IOException;

import client.Client;
import client.game.entity.Entity;
import client.game.world.Location;
import client.net.NetOpcodes;
import client.net.NetworkHandle;
import client.net.in.EntityMoveListener.EntityMovePacket;
import lombok.AllArgsConstructor;

public class EntityMoveListener extends PacketListener<EntityMovePacket> {

	@AllArgsConstructor
	class EntityMovePacket extends PacketData {
		private int entityId;
		private int tileX;
		private int tileY;
	}

	public EntityMoveListener() {
		super(NetOpcodes.ENTITY_MOVE);
	}

	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		int entityId = handle.readUnsignedInt24();
		int tileX = handle.readInt();
		int tileY = handle.readInt();
		return new EntityMovePacket(entityId, tileX, tileY);
	}

	@Override
	public void onEvent(EntityMovePacket packet) {
		
		Entity entity = Client.instance.getEntity(packet.entityId);
		if (entity == null) {
			return;
		}
		
		Location serverMove = new Location(packet.tileX, packet.tileY);
		
		if (entity.isPrimaryPlayer()) {
			if (entity.moveQueue.isEmpty()) {
				System.out.println("Move request with emprty queue for primary player?");
			} else {
				Location ourMove = entity.moveQueue.poll();
				
				if (!ourMove.equals(serverMove)) {
					entity.currentLocation = new Location(serverMove);
					entity.futureLocation = new Location(serverMove);
					// Clear the queue since it is invalid.
					entity.moveQueue.clear();
					System.out.println("MOVE QUEUE OUT OF SYNC");
				}
			}
		} else {
			entity.moveQueue.add(serverMove);
		}
	}
}
