package vork.server.net.out;

import lombok.Getter;
import vork.server.game.entity.Player;
import vork.server.net.NetworkHandle;

public abstract class PacketOut {
	
	@Getter
	private byte opcode;
	
	public PacketOut(byte opcode) {
		this.opcode = opcode;
	}
	
	public void send(NetworkHandle handle) {
		if (handle == null) return;
		
		handle.writeByte(opcode);
		writePacket(handle);
		handle.copyToAccBuffer();
	}
	
	public void send(Player player) {
		send(player.getNetworkHandle());
	}
	
	public abstract void writePacket(NetworkHandle handle);
	
}
