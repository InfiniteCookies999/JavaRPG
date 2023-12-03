package vork.server.net.in;

import java.io.IOException;

import lombok.Getter;
import vork.server.net.NetworkHandle;

public abstract class PacketListener <T extends PacketData> {

	@Getter
	private byte opcode;
	
	public PacketListener(byte opcode) {
		this.opcode = opcode;
	}
	
	public abstract PacketData decodePacket(NetworkHandle handle) throws IOException;
	
	public abstract void onEvent(final T packet);
	
}
