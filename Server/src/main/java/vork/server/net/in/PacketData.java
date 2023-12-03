package vork.server.net.in;

import lombok.Getter;
import lombok.Setter;
import vork.server.net.NetworkHandle;

@Getter
@Setter
public class PacketData {
	private byte opcode;
	private NetworkHandle handle;
}
