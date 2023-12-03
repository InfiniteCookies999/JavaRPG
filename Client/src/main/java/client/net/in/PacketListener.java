package client.net.in;

import java.io.IOException;

import client.net.NetworkHandle;
import lombok.Getter;

public abstract class PacketListener <T extends PacketData> {

	@Getter
	private byte opcode;
	
	public PacketListener(byte opcode) {
		this.opcode = opcode;
	}
	
	public abstract PacketData decodePacket(NetworkHandle handle) throws IOException;
	
	public abstract void onEvent(final T packet);
	
}
