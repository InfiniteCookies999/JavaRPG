package vork.server.net.out;

import vork.server.net.NetOpcodes;
import vork.server.net.NetworkHandle;

public class SetXpPacketOut extends PacketOut {

	private int type;
	private int xp;
	
	public SetXpPacketOut(int type, int xp) {
		super(NetOpcodes.SET_XP);
		this.type = type;
		this.xp = xp;
	}

	@Override
	public void writePacket(NetworkHandle handle) {
		handle.writeUnsignedByte(type);
		handle.writeInt(xp);
	}
}
