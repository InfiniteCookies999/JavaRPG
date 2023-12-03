package vork.server.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import lombok.Getter;
import lombok.Setter;
import vork.server.game.entity.Player;

public class NetworkHandle {

	@Getter
	private Socket socket;
	@Getter
	private DataInputStream inputStream;
	@Getter
	private DataOutputStream outputStream;
	
	@Setter @Getter
	private boolean inCharacterCreation = false;
	
	@Setter @Getter
	private int id;
	
	@Getter @Setter
	private Player player;
	
	// TODO MOVE (PROBABLY WANT TO SAVE/READ FROM DATABASE)
	public String playerName;
	public int gender;
	public int skinColor;
	public int hairStyle;
	public int hairColor;
	public int eyeColor;
	public int legsType;
	public int bodyType;
	
	private byte[] accBuffer = new byte[256];
	private int accBufferPos;
	
	private byte[] buffer = new byte[1024];
	private int bufferPos;
	
	public NetworkHandle(Socket socket, DataInputStream inputStream, DataOutputStream outputStream) {
		this.socket = socket;
		this.inputStream = inputStream;
		this.outputStream = outputStream;
	}
	
	public boolean hasLoggedIn() {
		return inCharacterCreation || player != null;
	}
	
	///
	// Reading
	///
	
	public boolean readBoolean() throws IOException {
		return inputStream.readBoolean();
	}
	
	public byte readByte() throws IOException {
		return inputStream.readByte();
	}
	
	public short readShort() throws IOException {
		return inputStream.readShort();
	}
	
	public int readInt() throws IOException {
		return inputStream.readInt();
	}
	
	public long readLong() throws IOException {
		return inputStream.readLong();
	}
	
	public int readUnsignedByte() throws IOException {
		return inputStream.readByte() & 0xFF;
	}
	
	public int readUnsignedShort() throws IOException {
		int b1 = inputStream.readByte();
		int b2 = inputStream.readByte();
		return ((b1 & 0xFF) << 8) | (b2 & 0xFF);
	}
	
	public int readUnsignedInt24() throws IOException {
		int b1 = inputStream.readByte();
		int b2 = inputStream.readByte();
		int b3 = inputStream.readByte();
		return ((b1 & 0xFF) << 16) | ((b2 & 0xFF) << 8) | (b3 & 0xFF);
	}
	
	public int readUnsignedInt() throws IOException {
		int b1 = inputStream.readByte();
		int b2 = inputStream.readByte();
		int b3 = inputStream.readByte();
		int b4 = inputStream.readByte();
		return ((b1 & 0xFF) << 24) | ((b2 & 0xFF) << 16) | ((b3 & 0xFF) << 8) | (b4 & 0xFF);
	}
	
	public String readSmallString() throws IOException {
		int length = readUnsignedByte();
		byte[] bytes = new byte[length];
		inputStream.read(bytes, 0, length);
		return new String(bytes, StandardCharsets.UTF_8);
	}
	
	public float readFloat() throws IOException {
		return inputStream.readFloat();
	}
	

	///
	// Writing
	///
	
	public void writeBoolean(boolean b) {
		buffer[bufferPos++] = (byte) (b ? 1 : 0);
	}
	
	public void writeByte(byte b) {
		buffer[bufferPos++] = b;
	}
	
	public void writeShort(short s) {
		buffer[bufferPos++] = (byte) ((s >> 8) & 0xFF);
		buffer[bufferPos++] = (byte) (s & 0xFF);
	}
	
	public void writeInt(int i) {
		buffer[bufferPos++] = (byte) ((i >> 24) & 0xFF);
		buffer[bufferPos++] = (byte) ((i >> 16) & 0xFF);
		buffer[bufferPos++] = (byte) ((i >> 8) & 0xFF);
		buffer[bufferPos++] = (byte) (i & 0xFF);
	}
	
	public void writeLong(long l) {
        buffer[bufferPos++] = (byte) ((l >>> 56) & 0xFF);
        buffer[bufferPos++] = (byte) ((l >>> 48) & 0xFF);
        buffer[bufferPos++] = (byte) ((l >>> 40) & 0xFF);
        buffer[bufferPos++] = (byte) ((l >>> 32) & 0xFF);
        buffer[bufferPos++] = (byte) ((l >>> 24) & 0xFF);
        buffer[bufferPos++] = (byte) ((l >>> 16) & 0xFF);
        buffer[bufferPos++] = (byte) ((l >>> 8) & 0xFF);
        buffer[bufferPos++] = (byte) (l & 0xFF);
    }
	
	// Ranges 0 to 255
	public void writeUnsignedByte(int b) {
		buffer[bufferPos++] = (byte) (b & 0xFF);
	}
	
	// Ranges 0 to 65,535
	public void writeUnsignedShort(int s) {
		buffer[bufferPos++] = (byte) ((s >> 8) & 0xFF);
		buffer[bufferPos++] = (byte) ((s >> 0) & 0xFF);
	}
	
	// Ranges 0 to 16,777,215
	public void writeUnsignedInt24(int s) {
		buffer[bufferPos++] = (byte) ((s >> 16) & 0xFF);
		buffer[bufferPos++] = (byte) ((s >> 8) & 0xFF);
		buffer[bufferPos++] = (byte) ((s >> 0) & 0xFF);
	}
	
	public void writeUnsignedInt(int s) {
		buffer[bufferPos++] = (byte) ((s >> 24) & 0xFF);
		buffer[bufferPos++] = (byte) ((s >> 16) & 0xFF);
		buffer[bufferPos++] = (byte) ((s >> 8) & 0xFF);
		buffer[bufferPos++] = (byte) ((s >> 0) & 0xFF);
	}
	
	public void writeFloat(float f) {
        writeInt(Float.floatToIntBits(f));
    }

	public void writeDouble(double d) {
        writeLong(Double.doubleToLongBits(d));
    }
	
	public void writeSmallString(final String s) {
		buffer[bufferPos++] = (byte) s.length();
		for (int i = 0; i < s.length(); i++) {
			buffer[bufferPos++] = (byte) s.charAt(i);
		}
	}
	
	public void copyToAccBuffer() {
		int remaining = bufferPos, o = 0;
		while (remaining > 0) {
			int space = 256 - accBufferPos; // how much space is available
			if (space >= remaining) {
				System.arraycopy(buffer, o, accBuffer, accBufferPos, remaining);
				accBufferPos += remaining;
				if (accBufferPos == 256) {
					flushBuffer(); // avoid ending on a full buffer
				}
				break;
			} else {
				// not enough to account for remaining so shoving what we have
				// into the available buffer space
				System.arraycopy(buffer, o, accBuffer, accBufferPos, space);
				accBufferPos += space;
				o += space;
				remaining -= space;
				flushBuffer();
			}	
		}
		bufferPos = 0;
	}
	
	public void flushBuffer() {
		if (accBufferPos > 0) {
			try {
				outputStream.write(accBuffer, 0, accBufferPos);
			} catch (IOException e) {
				// Silently ignore because this can happen
				// if the outputstream was already closed.
			}
			accBufferPos = 0;
		}
	}
	
	public void close() {
		try {
			socket.close();
			inputStream.close();
			outputStream.close();
		} catch (IOException e) {
			// Might have already been closed.
		}
	}
}
