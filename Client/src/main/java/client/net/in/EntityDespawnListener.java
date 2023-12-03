package client.net.in;

import java.io.IOException;

import client.Client;
import client.game.entity.Entity;
import client.net.NetOpcodes;
import client.net.NetworkHandle;
import client.net.in.EntityDespawnListener.EntityDespawnPacket;
import lombok.AllArgsConstructor;

public class EntityDespawnListener extends PacketListener<EntityDespawnPacket>{
	
	@AllArgsConstructor
	class EntityDespawnPacket extends PacketData {
		private int entityId;
	}
	
	public EntityDespawnListener() {
		super(NetOpcodes.ENTITY_DESPAWN);
	}

	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		
		int entityId = handle.readUnsignedInt24();
		
		return new EntityDespawnPacket(entityId);
	}

	@Override
	public void onEvent(EntityDespawnPacket packet) {
		Entity entity = Client.instance.entities.get(packet.entityId);
		if (entity != null) {
			entity.renderer.dispose();
			Client.instance.entities.remove(packet.entityId);	
		}
	}
	
}
