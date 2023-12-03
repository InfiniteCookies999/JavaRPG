package client.net.out;

import client.net.NetOpcodes;
import client.net.NetworkHandle;

public class ChatTextPacketOut extends PacketOut {

	private String message;
	
	public ChatTextPacketOut(String message) {
		super(NetOpcodes.CHAT_TEXT);
		this.message = message;
	}

	@Override
	public void writePacket(NetworkHandle handle) {
		handle.writeSmallString(message);
	}
}
