package vork.server.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.function.Consumer;

import lombok.Getter;
import vork.server.net.in.NetworkEventBus;

public class NetworkManager {

	private ServerSocket serverSocket;
	
	private Thread acceptConnectionsThread;
	
	@Getter
	private NetworkEventBus eventBus = new NetworkEventBus();
	
	@Getter
	private ConnectionManager connectionManager = new ConnectionManager();
	
	public boolean openServer(short port, Consumer<NetworkEventBus> eventBusSetupCallback) {
		eventBusSetupCallback.accept(eventBus);
		
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			return false;
		}
		
		System.out.println("Listening for connections on port: " + port);
		System.out.println();
		
		listenForConnections();
		
		return true;
	}
	
	private void listenForConnections() {
		acceptConnectionsThread = new Thread(() -> {
			
			while (!Thread.currentThread().isInterrupted()) {
				try {
					connectionManager.addConnection(serverSocket.accept());
				} catch (IOException e) {
					if (e instanceof SocketException) {
						break;
					}
					e.printStackTrace();
				}
			}
			
		}, "accept-connections-thread");
		acceptConnectionsThread.start();
	}
	
	public void disconnect(NetworkHandle handle) {
		connectionManager.addDisconnection(handle);
	}
	
	public void flushPackets() {
		connectionManager.flushPackets();
	}
	
	public void close() {
		try {
			serverSocket.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		acceptConnectionsThread.interrupt();
		try {
			acceptConnectionsThread.join();
		} catch (InterruptedException e) {
		}
		acceptConnectionsThread = null;
		connectionManager.close();
	}
}
