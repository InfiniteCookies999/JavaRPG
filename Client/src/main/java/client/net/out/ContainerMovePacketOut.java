package client.net.out;

import client.net.NetOpcodes;
import client.net.NetworkHandle;

public class ContainerMovePacketOut extends PacketOut {

	private int containerId;
	private int fromSlot;
	private int toSlot;
	
	public ContainerMovePacketOut(
			int containerId,
			int fromSlot, int toSlot
			) {
		super(NetOpcodes.CONTAINER_MOVE);
		this.containerId = containerId;
		this.fromSlot = fromSlot;
		this.toSlot = toSlot;
	}

	@Override
	public void writePacket(NetworkHandle handle) {
		handle.writeUnsignedByte(containerId);
		handle.writeUnsignedShort(fromSlot);
		handle.writeUnsignedShort(toSlot);
	}
}
