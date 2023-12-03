package client.net.out;

import client.net.NetOpcodes;
import client.net.NetworkHandle;

public class LoginPacketOut extends PacketOut {

	private final String username;
	
	public LoginPacketOut(final String username) {
		super(NetOpcodes.LOGIN);
		this.username = username;
	}

	@Override
	public void writePacket(NetworkHandle handle) {
		handle.writeSmallString(username);
	}
}
