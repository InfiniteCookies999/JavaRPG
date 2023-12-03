package client.net.in;

import java.io.IOException;

import client.Client;
import client.game.PlayScreen;
import client.game.ShaderProgramType;
import client.game.entity.Entity;
import client.game.entity.EntityHealthNumberRenderer;
import client.net.NetOpcodes;
import client.net.NetworkHandle;
import client.net.in.HealthChangeListener.HealthChangePacket;
import lombok.AllArgsConstructor;

public class HealthChangeListener extends PacketListener<HealthChangePacket> {

	@AllArgsConstructor
	class HealthChangePacket extends PacketData {
		private int entityId;
		private int changeInHealth;
		private int health;
		private boolean isPassiveHeal;
	}
	
	public HealthChangeListener() {
		super(NetOpcodes.HEALTH_CHANGE);
	}

	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		int entityId = handle.readUnsignedInt24();
		int changeInHealth = handle.readShort();
		int health = handle.readUnsignedInt24();
		boolean isPassiveHeal = false;
		if (changeInHealth > 0) {
			isPassiveHeal = handle.readBoolean();
		}
		
		return new HealthChangePacket(entityId, changeInHealth, health, isPassiveHeal);
	}

	@Override
	public void onEvent(HealthChangePacket packet) {
		Entity entity = Client.instance.getEntity(packet.entityId);
		if (entity == null) {
			return;
		}
		
		if (entity.isDead() && packet.health > 0) {
			// Entity was brought back to life!
			entity.renderer.setShaderProgramType(ShaderProgramType.DEFAULT);
		}
		
		if (!packet.isPassiveHeal) {
			EntityHealthNumberRenderer healthNumberRenderer = new EntityHealthNumberRenderer();
			healthNumberRenderer.changeInHealth = packet.changeInHealth;
			healthNumberRenderer.healthStartX = entity.x + entity.renderer.getEntityWidth() * 0.5f;
			healthNumberRenderer.healthStartY = entity.y + entity.renderer.getEntityHeight() * 0.25f;
			PlayScreen playScreen = Client.instance.getScreen();
			playScreen.addEntityHealthNumberRenderer(healthNumberRenderer);	
		}
		
		entity.setHealth(packet.health);
		entity.setChangeInHealth(packet.changeInHealth);
		
		if (entity.isDead()) {
			entity.renderer.setShaderProgramType(ShaderProgramType.ENTITY_DIED);
			if (entity.isPrimaryPlayer()) {
				PlayScreen playScreen = Client.instance.getScreen();
				playScreen.handlePlayerDeath();
			}
		} else if (packet.changeInHealth < 0) {
			entity.renderer.setShaderProgramType(ShaderProgramType.DEFAULT_ATTACKED);
		}		
	}
}
