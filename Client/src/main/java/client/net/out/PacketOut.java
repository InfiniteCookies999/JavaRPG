package client.net.out;

import client.Client;
import client.net.NetworkHandle;
import lombok.Getter;

public abstract class PacketOut {
	
	@Getter
	private byte opcode;
	
	public PacketOut(byte opcode) {
		this.opcode = opcode;
	}
	
	public void send() {
		NetworkHandle handle = Client.instance.getNetworkManager().getHandle();
		if (handle == null) return;
		
		handle.writeByte(opcode);
		writePacket(handle);
		handle.copyToAccBuffer();
	}
	
	public abstract void writePacket(NetworkHandle handle);
	
}
