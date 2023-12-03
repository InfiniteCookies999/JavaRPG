package vork.server.net.in;

import java.io.IOException;

import lombok.AllArgsConstructor;
import vork.server.game.LoginResponse;
import vork.server.net.NetOpcodes;
import vork.server.net.NetworkHandle;
import vork.server.net.in.LoginPacketListener.LoginPacket;
import vork.server.net.out.LoginPacketOut;

public class LoginPacketListener extends PacketListener<LoginPacket> {

	@AllArgsConstructor
	class LoginPacket extends PacketData {
		private String username;
	}
	
	public LoginPacketListener() {
		super(NetOpcodes.LOGIN);
	}

	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		
		String username = handle.readSmallString();
		
		return new LoginPacket(username);
	}

	@Override
	public void onEvent(LoginPacket packet) {
		if (packet.getHandle().hasLoggedIn()) return;
		
		System.out.println("login for username: " + packet.username);
		
		packet.getHandle().playerName = packet.username;
		
		new LoginPacketOut(LoginResponse.SEND_TO_CHARACTER_CREATION).send(packet.getHandle());
		packet.getHandle().setInCharacterCreation(true);
	}
}
