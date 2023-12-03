package vork.server.net.in;

import java.io.IOException;

import lombok.AllArgsConstructor;
import vork.server.Constants;
import vork.server.Server;
import vork.server.game.Location;
import vork.server.game.entity.Direction;
import vork.server.game.entity.Player;
import vork.server.game.world.World;
import vork.server.net.NetworkHandle;
import vork.server.net.in.MoveRequestListener.MoveRequestPacket;

public class MoveRequestListener extends PacketListener<MoveRequestPacket> {

	@AllArgsConstructor
	class MoveRequestPacket extends PacketData {
		private int tileX;
		private int tileY;
	}
	
	public MoveRequestListener() {
		super(vork.server.net.NetOpcodes.MOVE_REQUEST);
	}

	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		int tileX = handle.readInt();
		int tileY = handle.readInt();
		
		return new MoveRequestPacket(tileX, tileY);
	}

	@Override
	public void onEvent(MoveRequestPacket packet) {
		Player player = packet.getHandle().getPlayer();
		if (player == null) {
			return;
		}
		
		if (player.isDead()) {
			return;
		}
		
		World world = Server.instance.getWorld();
		if (!world.isTraversible(packet.tileX, packet.tileY)) {
			System.out.println("move request rejected because tile is not traversible");
			return;
		}
		
		if (player.moveQueueSize == Constants.MAX_MOVE_QUEUE_SIZE) {
			System.out.println("move request rejected because move queue size was too big");
			// TODO: Might want to tell the client they are lagging.
			return;
		}
		
		// Making sure the requested move location is actually 1 tile away from where
		// they are.
		if (player.moveQueueSize != 0) {
			Location lastLocation = player.moveQueue[player.moveQueueSize - 1];
			if (!isOneAway(lastLocation, packet.tileX, packet.tileY)) {
				System.out.println("move request not one tile away for movequeue > 0");
				return;
			}
		} else if (!player.currentLocation.equals(player.futureLocation)) {
			Location lastLocation = player.futureLocation;
			if (!isOneAway(lastLocation, packet.tileX, packet.tileY)) {
				System.out.println("move request not one tile away for current location != future location");
				return;
			}
		} else {
			Location lastLocation = player.currentLocation;
			if (!isOneAway(lastLocation, packet.tileX, packet.tileY)) {
				System.out.println("move request not one tile away for current location == future location");
				return;
			}
		}
		
		player.setGroundItemToPickup(-1);
		player.predictiveFacingDirection = Direction.NONE;
		player.moveQueue[player.moveQueueSize] = new Location(packet.tileX, packet.tileY);
		++player.moveQueueSize;
		
	}
	
	private boolean isOneAway(Location lastLocation, int tileX, int tileY) {
		int diffX = tileX - lastLocation.x;
		int diffY = tileY - lastLocation.y;
		return Math.abs(diffX) + Math.abs(diffY) == 1;
	}
}
