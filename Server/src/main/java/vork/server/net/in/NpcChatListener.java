package vork.server.net.in;

import java.io.IOException;

import lombok.AllArgsConstructor;
import vork.server.game.entity.Player;
import vork.server.net.NetOpcodes;
import vork.server.net.NetworkHandle;
import vork.server.net.in.NpcChatListener.NpcChatPacket;

public class NpcChatListener extends PacketListener<NpcChatPacket> {

	@AllArgsConstructor
	class NpcChatPacket extends PacketData {
		private int option;
	}
	
	public NpcChatListener() {
		super(NetOpcodes.NPC_CHAT);
	}

	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		int option = handle.readUnsignedByte();
		
		return new NpcChatPacket(option);
	}

	@Override
	public void onEvent(NpcChatPacket packet) {
		Player player = packet.getHandle().getPlayer();
		if (player == null || player.isDead()) {
			return;
		}
		
		player.continueNpcScript(packet.option);
	}
}
