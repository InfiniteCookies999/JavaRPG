package client.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import client.net.in.NetworkEventBus;
import lombok.Getter;

public class NetworkManager implements Runnable {

	private Socket socket;
	private DataInputStream inputStream;
	private DataOutputStream outputStream;
	
	@Getter
	private NetworkHandle handle;
	
	@Getter
	private NetworkEventBus eventBus = new NetworkEventBus();
	
	private volatile boolean isAttemptingConnection = false;
	private volatile boolean successfulConnection   = false;
	private volatile boolean readyToSendAndRecieve  = false;
	
	private Thread connectionThread;
	private Thread recievePacketsThread;
	
	public void connectToServer(String address, short port) {
		if (isAttemptingConnection) {
			System.out.println("tried to connect to the server twice");
			return;
		}
		isAttemptingConnection = true;
		connectionThread = new Thread(() -> {
			socket = new Socket();
			try {
				socket.connect(new InetSocketAddress(address, port), 1000 * 5);
			} catch (IOException e) {
				isAttemptingConnection = false;
				return;
			}
			
			// Trying to connect the streams
			try {
				inputStream = new DataInputStream(socket.getInputStream());
				outputStream = new DataOutputStream(socket.getOutputStream());
								
				handle = new NetworkHandle(inputStream, outputStream);
				
			} catch (IOException e) {
				isAttemptingConnection = false;
				return;
			}
			
			successfulConnection = true;
			
		}, "connect-thread-"+address);
		connectionThread.start();
	}
	
	public boolean isConnected() {
		return readyToSendAndRecieve;
	}
	
	public ConnectionStatus checkConnectionStatus() {
		if (readyToSendAndRecieve) return ConnectionStatus.SUCCESS; // Already fully setup.
		if (!isAttemptingConnection) {
			// Must have failed to connect.
			close();
			return ConnectionStatus.FAILED;
		}
		
		if (!successfulConnection)
			return ConnectionStatus.STILL_ATTEMPTING;
		
		isAttemptingConnection = false;
		readyToSendAndRecieve = true;
		new Thread(this, "recieve-packets-thread").start();
		
		return ConnectionStatus.SUCCESS;
	}
	
	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				byte opcode = handle.readByte();
				eventBus.networkThreadPublish(opcode, handle);
			} catch (IOException e) {
				readyToSendAndRecieve = false;
				break;
			}
		}
	}
	
	public void close() {
		try {
			if (connectionThread != null) {
				connectionThread.interrupt();
				connectionThread.join();
				connectionThread = null;
			}
			if (recievePacketsThread != null) {
				recievePacketsThread.interrupt();
				recievePacketsThread = null;
			}
			
			if (socket != null) socket.close();
			socket = null;
			if (inputStream != null) {
				inputStream.close();
			}
			if (outputStream != null) {
				outputStream.close();
			}
			inputStream = null;
			outputStream = null;
			handle = null;
			readyToSendAndRecieve = false;
			isAttemptingConnection = false;
			successfulConnection = false;
			eventBus.clear();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
		}
	}
}
