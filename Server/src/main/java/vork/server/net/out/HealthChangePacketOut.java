package vork.server.net.out;

import vork.server.game.entity.Entity;
import vork.server.net.NetOpcodes;
import vork.server.net.NetworkHandle;

public class HealthChangePacketOut extends PacketOut {

	private int entityId;
	private int changeInHealth;
	private int health;
	private boolean isPassiveHeal;
	
	public HealthChangePacketOut(Entity entity, int changeInHealth, boolean isPassiveHeal) {
		super(NetOpcodes.HEALTH_CHANGE);
		this.entityId = entity.id;
		this.changeInHealth = changeInHealth;
		this.health = entity.getHealth();
		this.isPassiveHeal = isPassiveHeal;
	}

	@Override
	public void writePacket(NetworkHandle handle) {
		handle.writeUnsignedInt24(entityId);
		handle.writeShort((short) changeInHealth);
		handle.writeUnsignedInt24(health);
		
		if (changeInHealth > 0) {
			handle.writeBoolean(isPassiveHeal);	
		}
	}
}
