package vork.server.net.out;

import vork.server.net.NetOpcodes;
import vork.server.net.NetworkHandle;

public class SetItemPacketOut extends PacketOut {

	private int itemId;
	private int amount;
	private int slot;
	
	public SetItemPacketOut(int itemId, int amount, int slot) {
		super(NetOpcodes.SET_ITEM);
		this.itemId = itemId;
		this.amount = amount;
		this.slot = slot;
	}

	@Override
	public void writePacket(NetworkHandle handle) {
		handle.writeUnsignedInt24(itemId);
		handle.writeInt(amount);
		handle.writeUnsignedByte(slot);
	}
}
