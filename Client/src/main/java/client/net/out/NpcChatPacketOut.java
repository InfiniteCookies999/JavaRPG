package client.net.out;

import client.net.NetOpcodes;
import client.net.NetworkHandle;

public class NpcChatPacketOut extends PacketOut {
	
	private int option;
	
	public NpcChatPacketOut(int option) {
		super(NetOpcodes.NPC_CHAT);
		this.option = option;
	}

	@Override
	public void writePacket(NetworkHandle handle) {
		handle.writeUnsignedByte(option);
	}
}
