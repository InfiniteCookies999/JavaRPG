package client.net.out;

import client.net.NetOpcodes;
import client.net.NetworkHandle;

public class CharacterCreatePacketOut extends PacketOut {

	private int gender;
	private int skinColor;
	private int hairStyle;
	private int hairColor;
	private int eyeColor;
	private int pantsStyle;
	private int pantsColor;
	private int shirtStyle;
	private int shirtColor;
	
	public CharacterCreatePacketOut(
			int gender, int skinColor, int hairStyle, int hairColor,
			int eyeColor, int pantsStyle, int pantsColor,
			int shirtStyle, int shirtColor) {
		super(NetOpcodes.CHARACTER_CREATE);
		this.gender = gender;
		this.skinColor = skinColor;
		this.hairStyle = hairStyle;
		this.hairColor = hairColor;
		this.eyeColor = eyeColor;
		this.pantsStyle = pantsStyle;
		this.pantsColor = pantsColor;
		this.shirtStyle = shirtStyle;
		this.shirtColor = shirtColor;
	}

	@Override
	public void writePacket(NetworkHandle handle) {
		int bits = hairColor << 11;
		bits |= hairStyle << 6;
		bits |= skinColor << 2;
		bits |= gender;
		handle.writeUnsignedShort(bits);
		handle.writeUnsignedByte(eyeColor);
		handle.writeUnsignedByte(pantsStyle);
		handle.writeUnsignedByte(pantsColor);
		handle.writeUnsignedByte(shirtStyle);
		handle.writeUnsignedByte(shirtColor);
	}
}
