package vork.server.net.in;

import java.io.IOException;

import lombok.AllArgsConstructor;
import vork.server.Constants;
import vork.server.game.entity.Player;
import vork.server.net.NetOpcodes;
import vork.server.net.NetworkHandle;
import vork.server.net.in.PickupItemListener.PickupItemPacket;

public class PickupItemListener extends PacketListener<PickupItemPacket> {

	@AllArgsConstructor
	class PickupItemPacket extends PacketData {
		private int groundItemId;
	}
	
	public PickupItemListener() {
		super(NetOpcodes.PICKUP_ITEM);
	}

	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		int groundItemId = handle.readUnsignedInt24();
		if (groundItemId >= Constants.MAX_GROUND_ITEMS) {
			return null;
		}
		
		return new PickupItemPacket(groundItemId);
	}

	@Override
	public void onEvent(PickupItemPacket packet) {
		Player player = packet.getHandle().getPlayer();
		if (player == null || player.isDead()) {
			return;
		}
		
		player.setGroundItemToPickup(packet.groundItemId);
		if (!player.isMoving()) {
			player.tryToPickupGroundItem();
		}
	}
}
