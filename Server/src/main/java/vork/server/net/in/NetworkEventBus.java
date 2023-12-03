package vork.server.net.in;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import vork.server.net.NetworkHandle;

public class NetworkEventBus {

	private final PacketListener<?>[] registeredListeners = new PacketListener[256];
	
	private final Queue<PacketData> decodedPackets = new ConcurrentLinkedQueue<>();
	
	public void registerListener(PacketListener<?> listener) {
		int index = (int) (listener.getOpcode() & 0xFF);
		if (registeredListeners[index] != null) {
			throw new IllegalStateException("Already registered listener for opcode: " + index);
		}
		registeredListeners[index] = listener;
	}
	
	public void networkThreadPublish(byte opcode, NetworkHandle handle) throws IOException {
		PacketListener<?> listener = registeredListeners[opcode & 0xFF];
		if (listener == null) {
			System.out.println("recieved unknown opcode: " + opcode + " over network.");
			return;
		}
		
		PacketData decodedPacket = listener.decodePacket(handle);
		if (decodedPacket == null) {
			handle.getSocket().close();
			return;
		}
		
		decodedPacket.setOpcode(opcode);
		decodedPacket.setHandle(handle);
		decodedPackets.add(decodedPacket);
	}
	
	@SuppressWarnings("unchecked")
	public void gameThreadPublish() {
		PacketData packetData;
		while ((packetData = decodedPackets.poll()) != null) {
			@SuppressWarnings("rawtypes")
			PacketListener listener = registeredListeners[packetData.getOpcode() & 0xFF];
			listener.onEvent(packetData);
		}
	}
}
