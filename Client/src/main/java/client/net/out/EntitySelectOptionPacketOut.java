package client.net.out;

import client.game.entity.Entity;
import client.net.NetOpcodes;
import client.net.NetworkHandle;

public class EntitySelectOptionPacketOut extends PacketOut {

	private int entityId;
	private int selectOption;
	
	public EntitySelectOptionPacketOut(Entity entity, int selectOption) {
		super(NetOpcodes.ENTITY_SELECT_OPTION);
		if (entity == null) {
			entityId = 0;
		} else {
			entityId = entity.id;	
		}
		this.selectOption = selectOption == 0 ? 0 : (int) (Math.log(selectOption) / Math.log(2)) + 1;
	}

	@Override
	public void writePacket(NetworkHandle handle) {
		handle.writeUnsignedInt24(entityId);
		handle.writeUnsignedByte(selectOption);
	}
}
