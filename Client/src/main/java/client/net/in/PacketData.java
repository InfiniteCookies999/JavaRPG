package client.net.in;

import client.net.NetworkHandle;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PacketData {
	private byte opcode;
	private NetworkHandle networkHandle;
}
