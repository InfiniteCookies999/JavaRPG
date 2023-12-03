package client.net.in;

import java.io.IOException;

import client.Client;
import client.game.LoginResponse;
import client.game.LoginScreen;
import client.net.NetOpcodes;
import client.net.NetworkHandle;
import client.net.in.LoginListener.LoginPacket;
import lombok.AllArgsConstructor;

public class LoginListener extends PacketListener<LoginPacket> {

	@AllArgsConstructor
	class LoginPacket extends PacketData {
		private LoginResponse response;
	}
	
	public LoginListener() {
		super(NetOpcodes.LOGIN);
	}

	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		
		byte loginResponseByte = handle.readByte();
		
		return new LoginPacket(LoginResponse.values()[loginResponseByte]);
	}

	@Override
	public void onEvent(LoginPacket packet) {
		LoginScreen screen = Client.instance.getScreen();
		screen.setLoginResponse(packet.response);
	}
}
