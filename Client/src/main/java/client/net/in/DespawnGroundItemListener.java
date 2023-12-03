package client.net.in;

import java.io.IOException;

import client.Client;
import client.game.PlayScreen;
import client.net.NetOpcodes;
import client.net.NetworkHandle;
import client.net.in.DespawnGroundItemListener.GroundItemPacket;
import lombok.AllArgsConstructor;

public class DespawnGroundItemListener extends PacketListener<GroundItemPacket> {

	@AllArgsConstructor
	class GroundItemPacket extends PacketData {
		private int id;
	}
	
	public DespawnGroundItemListener() {
		super(NetOpcodes.DESPAWN_GROUND_ITEM);
	}

	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		int id = handle.readUnsignedInt24();
		
		return new GroundItemPacket(id);
	}

	@Override
	public void onEvent(GroundItemPacket packet) {
		PlayScreen playScreen = Client.instance.getScreen();
		playScreen.removeGroundItem(packet.id);
	}
}
