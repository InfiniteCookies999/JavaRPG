package vork.server.net.out;

import vork.server.game.entity.AnimationType;
import vork.server.game.entity.Entity;
import vork.server.net.NetOpcodes;
import vork.server.net.NetworkHandle;

public class AnimationPacketOut extends PacketOut {

	private int entityId;
	private AnimationType type;
	
	public AnimationPacketOut(Entity entity, AnimationType type) {
		super(NetOpcodes.ANIMATION);
		this.entityId = entity.id;
		this.type = type;
	}

	@Override
	public void writePacket(NetworkHandle handle) {
		handle.writeUnsignedInt24(entityId);
		handle.writeUnsignedShort(type.ordinal());
	}
}
