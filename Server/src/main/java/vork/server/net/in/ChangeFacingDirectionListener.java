package vork.server.net.in;

import java.io.IOException;

import lombok.AllArgsConstructor;
import vork.server.game.entity.Direction;
import vork.server.game.entity.Player;
import vork.server.net.NetOpcodes;
import vork.server.net.NetworkHandle;
import vork.server.net.in.ChangeFacingDirectionListener.ChangeFacingDirectionPacket;
import vork.server.net.out.ChangeFacingDirectionPacketOut;

public class ChangeFacingDirectionListener extends PacketListener<ChangeFacingDirectionPacket> {

	@AllArgsConstructor
	class ChangeFacingDirectionPacket extends PacketData {
		private Direction direction;
	}
	
	public ChangeFacingDirectionListener() {
		super(NetOpcodes.CHANGE_FACING_DIRECTION);
	}
	
	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		int bits = handle.readUnsignedByte();
		if (bits == 0 || bits > 4) {
			// 0 does not count since it is NONE
			return null;
		}
		
		return new ChangeFacingDirectionPacket(Direction.values()[bits]);
	}

	@Override
	public void onEvent(ChangeFacingDirectionPacket packet) {
		Player player = packet.getHandle().getPlayer();
		if (player == null || player.isDead()) {
			return;
		}
		
		if (player.isMoving()) {
			player.predictiveFacingDirection = packet.direction;	
		} else {
			player.emitPacket(new ChangeFacingDirectionPacketOut(player, player.predictiveFacingDirection));
			player.setFacingDirection(packet.direction);
			player.predictiveFacingDirection = Direction.NONE;
		}
	}
}
