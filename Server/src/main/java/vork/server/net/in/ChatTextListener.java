package vork.server.net.in;

import java.io.IOException;

import lombok.AllArgsConstructor;
import vork.server.Constants;
import vork.server.game.ChatColors;
import vork.server.game.ChatTextType;
import vork.server.game.entity.Player;
import vork.server.net.NetOpcodes;
import vork.server.net.NetworkHandle;
import vork.server.net.in.ChatTextListener.ChatTextPacket;
import vork.server.net.out.ChatTextPacketOut;

public class ChatTextListener extends PacketListener<ChatTextPacket> {

	@AllArgsConstructor
	class ChatTextPacket extends PacketData {
		private String message;
	}
	
	public ChatTextListener() {
		super(NetOpcodes.CHAT_TEXT);
	}

	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		String message = handle.readSmallString();
		
		if (message.length() > Constants.MAX_CHAT_TEXT_LENGTH) {
			return null;
		}
		
		return new ChatTextPacket(message);
	}

	@Override
	public void onEvent(ChatTextPacket packet) {
		
		Player player = packet.getHandle().getPlayer();
		if (player == null) {
			return;
		}
		
		String message = ChatColors.GRAY + player.getName() + ": " + ChatColors.PUBLIC + packet.message;
		
		player.emitPacket(new ChatTextPacketOut(message, ChatTextType.PUBLIC, player.id));
		
	}
	
}
