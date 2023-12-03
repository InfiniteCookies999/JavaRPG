package client.net.in;

import java.io.IOException;

import client.Client;
import client.game.NpcChatOpcode;
import client.game.PlayScreen;
import client.game.entity.Entity;
import client.net.NetOpcodes;
import client.net.NetworkHandle;
import client.net.in.NpcChatListener.NpcChatPacket;
import lombok.AllArgsConstructor;

public class NpcChatListener extends PacketListener<NpcChatPacket> {

	@AllArgsConstructor
	class NpcChatPacket extends PacketData {
		private NpcChatOpcode chatOpcode;
		private int entityId;
		private String message;
		private String[] optionMessages;
	}
	
	public NpcChatListener() {
		super(NetOpcodes.NPC_CHAT);
	}

	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		NpcChatOpcode chatOpcode = NpcChatOpcode.values()[handle.readUnsignedByte()];
		int entityId = 0;
		String message = null;
		String[] optionMessages = null;
		if (chatOpcode == NpcChatOpcode.SAY) {
			entityId = handle.readUnsignedInt24();
			message = handle.readSmallString();	
		} else if (chatOpcode == NpcChatOpcode.OPTIONS_LIST) {
			entityId = handle.readUnsignedInt24();
			message = handle.readSmallString();	
			int numOptions = handle.readUnsignedByte();
			optionMessages = new String[numOptions];
			for (int i = 0; i < numOptions; i++) {
				optionMessages[i] = handle.readSmallString();
			}
		}
		
		return new NpcChatPacket(chatOpcode, entityId, message, optionMessages);
	}

	@Override
	public void onEvent(NpcChatPacket packet) {
		PlayScreen playScreen = Client.instance.getScreen();
		playScreen.getEntityFollower().setFollowingEntityId(-1);
		
		if (packet.chatOpcode == NpcChatOpcode.SAY) {
			Entity entity = Client.instance.getEntity(packet.entityId);
			if (entity != null) {
				playScreen.getNpcTalkMenu().say(entity, packet.message);	
			} else {
				playScreen.getNpcTalkMenu().setHidden(true);
			}
		} else if (packet.chatOpcode == NpcChatOpcode.OPTIONS_LIST) {
			Entity entity = Client.instance.getEntity(packet.entityId);
			if (entity != null) {
				playScreen.getNpcTalkMenu().options(entity, packet.message, packet.optionMessages);
			} else {
				playScreen.getNpcTalkMenu().setHidden(true);
			}
		} else if (packet.chatOpcode == NpcChatOpcode.END) {
			playScreen.getNpcTalkMenu().setHidden(true);
		}
	}
}
