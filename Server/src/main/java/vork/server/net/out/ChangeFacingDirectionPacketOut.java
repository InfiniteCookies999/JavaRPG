package vork.server.net.out;

import vork.server.game.entity.Direction;
import vork.server.game.entity.Entity;
import vork.server.net.NetOpcodes;
import vork.server.net.NetworkHandle;

public class ChangeFacingDirectionPacketOut  extends PacketOut {

	private int entityId;
	private Direction direction;
	
	public ChangeFacingDirectionPacketOut(Entity entity, Direction direction) {
		super(NetOpcodes.CHANGE_FACING_DIRECTION);
		this.entityId = entity.id;
		this.direction = direction;
	}

	@Override
	public void writePacket(NetworkHandle handle) {
		handle.writeUnsignedInt24(entityId);
		handle.writeUnsignedByte(direction.ordinal());
	}
}
