package vork.server.net.out;

import vork.server.game.ChatTextType;
import vork.server.net.NetOpcodes;
import vork.server.net.NetworkHandle;

public class ChatTextPacketOut extends PacketOut {

	private String message;
	private ChatTextType type;
	private int entityId;
	
	public ChatTextPacketOut(String message, ChatTextType type, int entityId) {
		super(NetOpcodes.CHAT_TEXT);
		this.message = message;
		this.type = type;
		this.entityId = entityId;
	}

	@Override
	public void writePacket(NetworkHandle handle) {
		handle.writeSmallString(message);
		handle.writeUnsignedByte(type.ordinal());
		if (type == ChatTextType.PUBLIC) {
			handle.writeUnsignedInt24(entityId);
		}
	}
}
