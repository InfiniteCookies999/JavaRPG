package vork.server.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import vork.server.Constants;
import vork.server.Server;
import vork.server.net.in.ClientSocketReciever;

public class ConnectionManager {

	private Queue<Socket> connectingSockets = new ConcurrentLinkedQueue<>();
	private Queue<NetworkHandle> disconnectingHandles = new ConcurrentLinkedQueue<>();
	
	// These do not need to be concurrent since they are updated on the main thread.
	private NetworkHandle[] acceptedConnections = new NetworkHandle[Constants.MAX_ALLOWED_CLIENTS];
	private Queue<Integer> availableClientIdSlots = new LinkedList<>();
	
	public ConnectionManager() {
		for (int i = 0; i < Constants.MAX_ALLOWED_CLIENTS; i++) {
			availableClientIdSlots.add(i);
		}
	}
	
	public void addConnection(Socket connection) {
		connectingSockets.add(connection);
	}
	
	public void addDisconnection(NetworkHandle handle) {
		disconnectingHandles.add(handle);
	}
	
	public void processConnections() {
		Socket socket = null;
		while ((socket = connectingSockets.poll()) != null) {
			processConnection(socket);
		}
	}
	
	private void processConnection(Socket socket) {
		NetworkHandle handle = null;
		DataOutputStream outStream = null;
		DataInputStream inStream = null;
		try {
			outStream = new DataOutputStream(socket.getOutputStream());
			inStream = new DataInputStream(socket.getInputStream());
		
			handle = new NetworkHandle(socket, inStream, outStream);
	        if (!validateNewConnection(handle)) {
	        	handle.close();
	        	return;
	        }
		} catch (IOException e) {
			if (socket != null) {
				try {
					socket.close();
					if (outStream != null) {
						outStream.close();	
					}
					// TODO: why is this complaining?
					if (inStream != null) {
						inStream.close();	
					}
				} catch (IOException e1) {
				}
			}
			return;
		}
		
		
		int networkId = availableClientIdSlots.poll();
		acceptedConnections[networkId] = handle;
		handle.setId(networkId);
		
		// A Thread per client to listen for incoming packets
		new Thread(new ClientSocketReciever(handle),
				"recieve-packets-" + socket.getInetAddress().getHostAddress())
		.start();
	}
	
	private boolean validateNewConnection(NetworkHandle handle) {
		 if (availableClientIdSlots.isEmpty()) {
			 // Too many clients already connected.
			 return false;
		 }
		 return true;
	}
	
	public void processDisconnections() {
		NetworkHandle handle = null;
		while ((handle = disconnectingHandles.poll()) != null) {
			processDisconnection(handle);
		}
	}
	
	private void processDisconnection(NetworkHandle handle) {
		handle.close();
		// Free the slot for other clients to login.
		acceptedConnections[handle.getId()] = null;
		availableClientIdSlots.add(handle.getId());
		Server.instance.getDisconnectionService().onDisconnection(handle);
	}
	
	public void flushPackets() {
		for (NetworkHandle handle : acceptedConnections) {
			if (handle != null) {
				handle.flushBuffer();
			}
		}
	}
	
	public void close() {
		// Close all the sockets that were never able to fully connect.
		for (Socket socket : connectingSockets) {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
		// Close the currently accepted connections.
		for (NetworkHandle handle : acceptedConnections) {
			if (handle != null) {
				handle.close();	
			}
		}
	}
}
