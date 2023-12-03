package vork.server.net.in;

import java.io.IOException;

import vork.server.Server;
import vork.server.net.NetworkHandle;
import vork.server.net.NetworkManager;

public class ClientSocketReciever implements Runnable {

	private NetworkHandle handle;
	private NetworkManager networkManager;
	
	public ClientSocketReciever(NetworkHandle handle) {
		this.handle = handle;
		networkManager =  Server.instance.getNetworkManager();
	}
	
	@Override
	public void run() {
		 try {
			 while (true) {
				 
				 byte opcode = handle.getInputStream().readByte();
				 
				 networkManager.getEventBus()
				               .networkThreadPublish(opcode, handle);
				 
			 }
		 } catch (IOException e) {
			 networkManager.disconnect(handle);
		 } finally {
			 handle.close();
		 }
	}

}
