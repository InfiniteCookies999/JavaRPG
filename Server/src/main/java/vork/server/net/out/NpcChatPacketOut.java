package vork.server.net.out;

import vork.server.game.NpcChatOpcode;
import vork.server.net.NetOpcodes;
import vork.server.net.NetworkHandle;

public class NpcChatPacketOut extends PacketOut {

	private NpcChatOpcode chatOpcode;
	private int entityId;
	private String message;
	private String[] optionMessages;
	
	private NpcChatPacketOut() {
		super(NetOpcodes.NPC_CHAT);
	}
	
	public static NpcChatPacketOut say(int entityId, String message) {
		NpcChatPacketOut packet = new NpcChatPacketOut();
		packet.chatOpcode = NpcChatOpcode.SAY;
		packet.entityId = entityId;
		packet.message = message;
		return packet;
	}
	
	public static NpcChatPacketOut options(int entityId, String message, String[] optionMessages) {
		NpcChatPacketOut packet = new NpcChatPacketOut();
		packet.chatOpcode = NpcChatOpcode.OPTIONS_LIST;
		packet.entityId = entityId;
		packet.message = message;
		packet.optionMessages = optionMessages;
		return packet;
	}
	
	public static NpcChatPacketOut end() {
		NpcChatPacketOut packet = new NpcChatPacketOut();
		packet.chatOpcode = NpcChatOpcode.END;
		return packet;
	}
	
	@Override
	public void writePacket(NetworkHandle handle) {
		handle.writeUnsignedByte(chatOpcode.ordinal());
		if (chatOpcode == NpcChatOpcode.SAY) {
			handle.writeUnsignedInt24(entityId);
			handle.writeSmallString(message);	
		} else if (chatOpcode == NpcChatOpcode.OPTIONS_LIST) {
			handle.writeUnsignedInt24(entityId);
			handle.writeSmallString(message);
			handle.writeUnsignedByte(optionMessages.length);
			for (int i = 0; i < optionMessages.length; i++) {
				handle.writeSmallString(optionMessages[i]);
			}
		} else if (chatOpcode == NpcChatOpcode.END) {
			
		}
	}
}
