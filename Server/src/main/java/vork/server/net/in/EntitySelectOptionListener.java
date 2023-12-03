package vork.server.net.in;

import java.io.IOException;

import lombok.AllArgsConstructor;
import vork.server.Server;
import vork.server.game.SelectOptions;
import vork.server.game.entity.Entity;
import vork.server.game.entity.Npc;
import vork.server.game.entity.Player;
import vork.server.net.NetOpcodes;
import vork.server.net.NetworkHandle;
import vork.server.net.in.EntitySelectOptionListener.SelectOptionPacket;

public class EntitySelectOptionListener extends PacketListener<SelectOptionPacket> {

	public EntitySelectOptionListener() {
		super(NetOpcodes.ENTITY_SELECT_OPTION);
	}

	@AllArgsConstructor
	class SelectOptionPacket extends PacketData {
		private int entityId;
		private int selectOption;
	}

	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		int entityId = handle.readUnsignedInt24();
		int selectOption = handle.readUnsignedByte();
		return new SelectOptionPacket(entityId, selectOption);
	}

	@Override
	public void onEvent(SelectOptionPacket packet) {
		Player player = packet.getHandle().getPlayer();
		if (player == null) {
			System.out.println("null player");
			return;
		}
		
		// Cancel whatever the player is currently doing.
		player.cancelAction();
		
		if (packet.selectOption == 0) {
			return;
		}
		
		if (player.isDead()) {
			return;
		}
		
		int selectOption = 1 << (packet.selectOption - 1);
		
		Entity selectedEntity = Server.instance.getEntity(packet.entityId);
		if (selectedEntity == null || selectedEntity == player || selectedEntity.isDead()) {
			return;
		}
		
		if ((selectedEntity.selectOptions & selectOption) == 0) {
			// The entity does not have the given option.
			return;
		}
		
		switch (selectOption) {
		case SelectOptions.ATTACK:
			player.targetEntity(selectedEntity);	
			break;
		case SelectOptions.TALK_TO:
			if (selectedEntity instanceof Npc) {
				Npc npcToTalkTo = (Npc) selectedEntity;
				player.setNpcToTalkTo(npcToTalkTo);
			}
			break;
		}
	}
}
