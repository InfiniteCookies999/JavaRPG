package client.net.out;

import client.net.NetOpcodes;
import client.net.NetworkHandle;

public class DeathConfirmationPacketOut extends PacketOut {

	public DeathConfirmationPacketOut() {
		super(NetOpcodes.DEATH_CONFIRMATION);
	}

	@Override
	public void writePacket(NetworkHandle handle) {
		// Nothing to write except the opcode to confirm death.
	}
}
