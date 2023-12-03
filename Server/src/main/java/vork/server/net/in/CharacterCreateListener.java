package vork.server.net.in;

import java.io.IOException;

import lombok.AllArgsConstructor;
import vork.server.Constants;
import vork.server.Server;
import vork.server.game.entity.HumanRenderData;
import vork.server.net.NetOpcodes;
import vork.server.net.NetworkHandle;
import vork.server.net.in.CharacterCreateListener.CharacterCreatePacket;

public class CharacterCreateListener extends PacketListener<CharacterCreatePacket> {

	public CharacterCreateListener() {
		super(NetOpcodes.CHARACTER_CREATE);
	}

	@AllArgsConstructor
	class CharacterCreatePacket extends PacketData {
		int gender;
		int skinColor;
		int hairStyle;
		int hairColor;
		int eyeColor;
		int pantsStyle;
		int pantsColor;
		int shirtStyle;
		int shirtColor;
	}

	@Override
	public PacketData decodePacket(NetworkHandle handle) throws IOException {
		int bits = handle.readUnsignedShort();
		int eyeColor = handle.readUnsignedByte();
		int pantsStyle = handle.readUnsignedByte();
		int pantsColor = handle.readUnsignedByte();
		int shirtStyle = handle.readUnsignedByte();
		int shirtColor = handle.readUnsignedByte();
		
		// |------------------|------------------|------------------|------------------|
		// | hair color (5 b) | hair style (5 b) | skin color (4 b) | gender     (2 b) |
		// |------------------|------------------|------------------|------------------|
		
		int gender = bits & 3;
		int skinColor = (bits >> 2) & 15;
		int hairStyle = (bits >> 6) & 31;
		int hairColor = (bits >> 11) & 31;
		
		if (gender > 2 ||
			skinColor > Constants.NUM_HUMAN_SKIN_COLORS ||
			hairStyle > Constants.NUM_HUMAN_HAIR_STYLES ||
			hairColor > Constants.NUM_HUMAN_HAIR_COLORS ||
			eyeColor > Constants.NUM_HUMAN_EYE_COLORS ||
			pantsStyle > Constants.NUM_HUMAN_PANTS_STYLES ||
			pantsColor > Constants. NUM_HUMAN_CLOTHES_COLORS ||
			shirtStyle > Constants.NUM_HUMAN_SHIRT_STYLES ||
			shirtColor > Constants.NUM_HUMAN_CLOTHES_COLORS
			) {
			return null;
		}
		
		return new CharacterCreatePacket(
				gender,
				skinColor,
				hairStyle,
				hairColor,
				eyeColor,
				pantsStyle,
				pantsColor,
				shirtStyle,
				shirtColor);
	}

	@Override
	public void onEvent(CharacterCreatePacket packet) {
		
		if (!packet.getHandle().isInCharacterCreation()) {
			return;
		}
		
		NetworkHandle handle = packet.getHandle();
		
		handle.gender     = packet.gender;    
		handle.skinColor  = packet.skinColor; 
		handle.hairStyle  = packet.hairStyle; 
		handle.hairColor  = packet.hairColor; 
		handle.eyeColor   = packet.eyeColor;  
		handle.legsType   = HumanRenderData.getHumanPantsType(packet.pantsStyle, packet.pantsColor);
		handle.bodyType   = HumanRenderData.getHumanShirtType(packet.shirtStyle, packet.shirtColor);
		
		handle.setInCharacterCreation(false);
		Server.instance.requestPlayerJoin(packet.getHandle());
		
	}
	
}
