package client.net.in;

import java.io.IOException;

import client.Client;
import client.game.PlayScreen;
import client.game.items.GroundItem;
import client.net.NetOpcodes;
import client.net.NetworkHandle;
import client.net.in.SpawnGroundItemListener.SpawnItemPacket;
import lombok.AllArgsConstructor;

public class SpawnGroundItemListener extends PacketListener<SpawnItemPacket> {

	@AllArgsConstructor
	class SpawnItemPacket extends PacketData {
		private int id;
		private int itemId;
		private int tileX, tileY;
	}
	
	public SpawnGroundItemListener() {
		super(NetOpcodes.SPAWN_GROUND_ITEM);
	}

	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		int id = handle.readUnsignedInt24();
		int itemId = handle.readUnsignedInt24();
		int tileX = handle.readInt();
		int tileY = handle.readInt();
		
		return new SpawnItemPacket(id, itemId, tileX, tileY);
	}

	@Override
	public void onEvent(SpawnItemPacket packet) {
		GroundItem groundItem = new GroundItem(packet.id, packet.tileX, packet.tileY, packet.itemId);
		PlayScreen playScreen = Client.instance.getScreen();
		playScreen.addGroundItem(packet.id, groundItem);
	}
}
