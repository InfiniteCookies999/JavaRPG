package client.net.in;

import java.io.IOException;

import client.Client;
import client.game.PlayScreen;
import client.game.entity.Entity;
import client.game.menus.TextChatMenu;
import client.game.menus.container.InventoryMenu;
import client.game.skills.Skill;
import client.game.skills.SkillType;
import client.net.NetOpcodes;
import client.net.NetworkHandle;
import client.net.in.SetXpListener.GiveXpPacket;
import lombok.AllArgsConstructor;
import vork.gfx.Color;
import vork.gfx.gui.drag.GuiDragAndDrop;

public class SetXpListener extends PacketListener<GiveXpPacket> {

	@AllArgsConstructor
	class GiveXpPacket extends PacketData {
		private int type;
		private int xp;
	}
	
	public SetXpListener() {
		super(NetOpcodes.SET_XP);
	}

	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		int type = handle.readUnsignedByte();
		int xp = handle.readInt();
		
		return new GiveXpPacket(type, xp);
	}

	@Override
	public void onEvent(GiveXpPacket packet) {
		
		// TODO: Move this!!!
		if (Client.instance.inventory == null) {
			Client.instance.containersDragAndDrop = new GuiDragAndDrop();
			Client.instance.inventory = new InventoryMenu(Client.instance.containersDragAndDrop);
		}
		
		Entity player = Client.instance.player;
		if (player == null) {
			// Initial experience.
			Skill skill = new Skill();
			skill.give(packet.xp);
			Client.instance.skills[packet.type] = skill;
			return;
		}
		
		PlayScreen playScreen = Client.instance.getScreen();
		TextChatMenu textChatMenu = playScreen.getTextChatMenu();
		
		String skillName = SkillType.getName(packet.type);
		Skill skill = Client.instance.skills[packet.type];
		
		int xpGained = skill.getXp() - packet.xp;
		if (xpGained >= 0) {
			textChatMenu.addMessage(String.format("you gained %s %s xp.", skillName, xpGained), Color.WHITE);
			
			int oldLevel = skill.getLevel();
			int levelsGained = skill.give(xpGained);
			if (levelsGained > 0) {
				for (int i = 0; i < levelsGained; i++) {
					textChatMenu.addMessage(
							String.format(
								"you levled up %s! You are now level %s!",
								skillName,
								oldLevel + i + 1),
							Color.WHITE);
				}
			}	
		} else {
			skill.setXp(xpGained);
		}
	}
}
