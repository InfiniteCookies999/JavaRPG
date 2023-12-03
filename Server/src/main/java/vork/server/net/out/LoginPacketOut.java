package vork.server.net.out;

import vork.server.game.LoginResponse;
import vork.server.net.NetOpcodes;
import vork.server.net.NetworkHandle;

public class LoginPacketOut extends PacketOut {
	
	private LoginResponse response;
	
	public LoginPacketOut(LoginResponse response) {
		super(NetOpcodes.LOGIN);
		this.response = response;
	}

	@Override
	public void writePacket(NetworkHandle handle) {
		handle.writeByte((byte) response.ordinal());
	}
}
