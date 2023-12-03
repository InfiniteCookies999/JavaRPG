package client.net.in;

import java.io.IOException;

import client.Client;
import client.game.PlayScreen;
import client.game.entity.AnimationType;
import client.game.entity.Entity;
import client.game.entity.LevelUpRenderer;
import client.net.NetOpcodes;
import client.net.NetworkHandle;
import client.net.in.AnimationListener.AnimationPacket;
import lombok.AllArgsConstructor;

public class AnimationListener extends PacketListener<AnimationPacket> {

	@AllArgsConstructor
	class AnimationPacket extends PacketData {
		private int entityId;
		private AnimationType type;
	}
	
	public AnimationListener() {
		super(NetOpcodes.ANIMATION);
	}

	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		int entityId = handle.readUnsignedInt24();
		AnimationType type = AnimationType.values()[handle.readUnsignedShort()];
		
		return new AnimationPacket(entityId, type);
	}

	@Override
	public void onEvent(AnimationPacket packet) {
		Entity entity = Client.instance.getEntity(packet.entityId);
		if (entity == null) {
			return;
		}
		
		PlayScreen playScreen = Client.instance.getScreen();
		if (packet.type == AnimationType.LEVEL_UP) {
			
			LevelUpRenderer levelUpRenderer = new LevelUpRenderer();
			levelUpRenderer.startX = entity.x + entity.renderer.getEntityWidth()/2;
			levelUpRenderer.startY = entity.y + entity.renderer.getEntityHeight()/2 + 5;
			
			playScreen.addLevelUpRenderer(levelUpRenderer);
		}
	}
}
