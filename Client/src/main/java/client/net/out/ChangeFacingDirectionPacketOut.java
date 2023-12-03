package client.net.out;

import client.game.entity.Direction;
import client.net.NetOpcodes;
import client.net.NetworkHandle;

public class ChangeFacingDirectionPacketOut extends PacketOut {

	private Direction direction;
	
	public ChangeFacingDirectionPacketOut(Direction direction) {
		super(NetOpcodes.CHANGE_FACING_DIRECTION);
		this.direction = direction;
	}

	@Override
	public void writePacket(NetworkHandle handle) {
		handle.writeUnsignedByte(direction.ordinal());
	}
}
