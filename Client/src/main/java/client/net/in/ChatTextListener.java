package client.net.in;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import client.Client;
import client.Constants;
import client.game.ChatTextType;
import client.game.PlayScreen;
import client.game.entity.Entity;
import client.game.entity.PublicChatMessageRenderer;
import client.game.menus.TextChatMenu;
import client.net.NetOpcodes;
import client.net.NetworkHandle;
import client.net.in.ChatTextListener.ChatTextPacket;
import lombok.AllArgsConstructor;
import vork.gfx.Color;
import vork.gfx.gui.GuiTextFormat;

public class ChatTextListener extends PacketListener<ChatTextPacket> {

	public ChatTextListener() {
		super(NetOpcodes.CHAT_TEXT);
	}

	@AllArgsConstructor
	class ChatTextPacket extends PacketData {
		private byte[] message;
		private ChatTextType type;
		private int entityId;
	}

	private Color[] colorCodeMap = {
			Color.RED,   
			Color.GREEN,
			Color.BLUE,
			TextChatMenu.PUBLIC_CHAT_COLOR,
			Color.WHITE, 
			Color.rgb(0xcacbcc)
	};
	
	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		byte[] message = handle.readStringBytes();
		ChatTextType type = ChatTextType.values()[handle.readUnsignedByte()];
		int entityId = 0;
		if (type == ChatTextType.PUBLIC) {
			entityId = handle.readUnsignedInt24();
		}
		
		return new ChatTextPacket(message, type, entityId);
	}

	private void buildMessage(byte[] message, Color curColor) {
		PlayScreen playScreen = Client.instance.getScreen();
		
		List<GuiTextFormat> formattedLine = new ArrayList<>();
		String curMessage = "";
		for (int i = 0; i < message.length; i++) {
			byte c = message[i];
			if (c == Constants.CHAT_COLOR_CODE) {
				byte nc = (byte) (message[i+1] - '0');
				formattedLine.add(new GuiTextFormat(curMessage, curColor, false));
				curColor = colorCodeMap[nc];
				curMessage = "";
				++i; // skip color code
			} else {
				curMessage += (char) c;
			}
		}
		if (!curMessage.isEmpty()) {
			formattedLine.add(new GuiTextFormat(curMessage, curColor, false));
		}
		
		playScreen.getTextChatMenu().addMessage(formattedLine);
	}
	
	private static String stripColorCodes(byte[] message) {
		String newString = "";
		for (int i = 0; i < message.length; i++) {
			byte c = message[i];
			if (c == Constants.CHAT_COLOR_CODE) {
				++i; // skip color code
			} else {
				newString += (char) c;
			}
		}
		return newString;
	}
	
	@Override
	public void onEvent(ChatTextPacket packet) {
		PlayScreen playScreen = Client.instance.getScreen();
		
		if (packet.type == ChatTextType.PUBLIC) {
			Entity entity = Client.instance.getEntity(packet.entityId);
			if (entity != null) {
				
				String noNameMessage = stripColorCodes(packet.message).split(":")[1].substring(1);
				
				PublicChatMessageRenderer renderer = new PublicChatMessageRenderer();
				renderer.entity = entity;
				renderer.message = noNameMessage;
				playScreen.addPublicChatMessageRenderer(renderer);
			}
			
			buildMessage(packet.message, TextChatMenu.PUBLIC_CHAT_COLOR);
		} else if (packet.type == ChatTextType.SERVER) {
			buildMessage(packet.message, TextChatMenu.SERVER_CHAT_COLOR);
		}
	}
}
