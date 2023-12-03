package client.net.in;

import java.io.IOException;

import client.Client;
import client.game.entity.Direction;
import client.game.entity.Entity;
import client.net.NetOpcodes;
import client.net.NetworkHandle;
import client.net.in.ChangeFacingDirectionListener.ChangeFacingDirectionPacket;
import lombok.AllArgsConstructor;

public class ChangeFacingDirectionListener extends PacketListener<ChangeFacingDirectionPacket> {

	@AllArgsConstructor
	class ChangeFacingDirectionPacket extends PacketData {
		private int entityId;
		private Direction direction;
	}
	
	public ChangeFacingDirectionListener() {
		super(NetOpcodes.CHANGE_FACING_DIRECTION);
	}
	
	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		int entityId = handle.readUnsignedInt24();
		int bits = handle.readUnsignedByte();
		
		return new ChangeFacingDirectionPacket(entityId, Direction.values()[bits]);
	}

	@Override
	public void onEvent(ChangeFacingDirectionPacket packet) {
		Entity entity = Client.instance.getEntity(packet.entityId);
		if (entity == null || entity == Client.instance.player) {
			return;
		}
		
		if (!entity.isMoving()) {
			entity.setFacingDirection(packet.direction);
		} else {
			entity.setFutureFacingDirection(packet.direction);
		}
	}
}