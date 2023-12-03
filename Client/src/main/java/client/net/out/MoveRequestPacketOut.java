package client.net.out;

import client.net.NetOpcodes;
import client.net.NetworkHandle;

public class MoveRequestPacketOut extends PacketOut {

	private int tileX;
	private int tileY;
	
	public MoveRequestPacketOut(int tileX, int tileY) {
		super(NetOpcodes.MOVE_REQUEST);
		this.tileX = tileX;
		this.tileY = tileY;
	}

	@Override
	public void writePacket(NetworkHandle handle) {
		handle.writeInt(tileX);
		handle.writeInt(tileY);
	}
}
