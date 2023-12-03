package vork.server.net.in;

import java.io.IOException;

import vork.server.game.entity.Player;
import vork.server.net.NetOpcodes;
import vork.server.net.NetworkHandle;
import vork.server.net.in.DeathConfirmationListener.DeathConfirmationPacket;

public class DeathConfirmationListener extends PacketListener<DeathConfirmationPacket> {

	class DeathConfirmationPacket extends PacketData { 
		
	}
	
	public DeathConfirmationListener() {
		super(NetOpcodes.DEATH_CONFIRMATION);
	}

	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		return new DeathConfirmationPacket();
	}

	@Override
	public void onEvent(DeathConfirmationPacket packet) {
		Player player = packet.getHandle().getPlayer();
		if (player == null || !player.isDead()) {
			return;
		}
		
		// We can now respawn the player
		player.respawn();
	}
	
}
