package vork.gfx;


import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import vork.Disposable;
import vork.util.FilePath;

@AllArgsConstructor
@Getter
public class Texture implements Disposable {

	@Getter
	@AllArgsConstructor
	public static class Buffer {
		private int width;
		private int height;
		private int[] pixels;
	
		public Buffer() {
			
		}
		
		public ByteBuffer asByteBuffer() {
			if (pixels == null) return null;
			ByteBuffer byteBuffer = ByteBuffer.allocateDirect(
					(width * height) << 2);
			for (int pixel : pixels) {
				byteBuffer.put((byte) ((pixel>>16)&0xFF));
				byteBuffer.put((byte) ((pixel>>8)&0xFF));
				byteBuffer.put((byte) ((pixel)&0xFF));
				byteBuffer.put((byte) ((pixel>>24)&0xFF));
			}
			
			byteBuffer.flip();
			return byteBuffer;
		}
	}
	
	private int glId;
	private int width;
	private int height;
	@Getter
	private TextureFilter filter;
	
	public void exchange(Texture newTexture) {
		this.glId = newTexture.glId;
		this.width = newTexture.width;
		this.height = newTexture.height;
		this.filter = newTexture.filter;
	}
	
	private static int scaleTo2N(int size) {
		if (size <= 2) {
			return 2;
		} else if (size > 2 && size <= 4) {
			return 4;
		} else if (size > 4 && size <= 8) {
			return 8;
		} else if (size > 8 && size <= 16) {
			return 16;
		} else if (size > 16 && size <= 32) {
			return 32;
		} else if (size > 32 && size <= 64) {
			return 64;
		} else if (size > 64 && size <= 128) {
			return 128;
		} else if (size > 128 && size <= 256) {
			return 256;
		} else if (size > 256 && size <= 512) {
			return 512;
		} else if (size > 512 && size <= 1024) {
			return 1024;
		} else if (size > 1024 && size <= 2048) {
			return 2048;
		} else if (size > 2048 && size <= 4096) {
			return 4096;
		} else if (size > 4096 && size <= 8192) {
			return 8192;
		} else if (size > 8192 && size <= 16384) {
			return 16384;
		} else if (size > 16384 && size <= 32768) {
			return 32768;
		}
		return size;
	}
	
	// Not sure this works for anything besides 16x16 tiles
	private static int[] padPixels(Buffer b, int tW, int tH) {
		// lol this shit took me way too long :D
		int nXTiles = b.getWidth()/tW;
		int nYTiles = b.getHeight()/tH;
		int oWidth = b.getWidth() + nXTiles*2;
		int oHeight = b.getHeight() + nYTiles*2;
		int w = scaleTo2N(oWidth);
		int h = scaleTo2N(oHeight);
		int ww = b.getWidth();
		int[] inPixels = b.getPixels();
		int[] outPixels = new int[w*h];
		// sh is used to shift texture to the bottom of the image.
		int sh = w*h - w*oHeight;
		for (int y = 0; y < nYTiles; y++) {
			int xScan = 0; // top/bottom pixel index across row.
			int xo = 0;
			for (int x = 0; x < oWidth; x++) { // applies padding to top of tile.
				int ip = inPixels[(y*ww*tH)+xScan]; // y*ww  <<= index on Y.  Then *tH move by height
				if (x == xo-1) ip = inPixels[(y*ww*tH)+xScan-1];
				if (x != xo-1 && x != xo) ++xScan;
				if (x == xo) xo += tW + 2;
				outPixels[y*(tH+2)*w+x + sh] = ip;
			}
			xo = 0;
			xScan = 0;
			for (int x = 0; x < oWidth; x++) {// applies padding to bottom of tile.
				int ip = 0;
				if (x == xo-1) ip = inPixels[((y+1)*tH-1)*ww+xScan-1];
				else ip = inPixels[((y+1)*tH-1)*ww+xScan];
				if (x != xo-1 && x != xo) ++xScan;
				if (x == xo) xo += tW + 2;
				outPixels[((y+1)*(tH+2)-1)*w+x + sh] = ip;
			}
			// drawing the tiles for the row and left/right pad
			for (int x = 0; x < nXTiles; x++) {
				int sy = (y*(tH+2) + 1);
				int sx = 1 + (tW+2)*x;
				// padding
				for (int yy = 0; yy < tH; yy++) {
					// left pad
					int ip0 = inPixels[y*tH*ww + x*tW + yy*ww];
					outPixels[sy*w + yy*w + sx - 1 + sh] = ip0;
					// right pad
					int ip1 = inPixels[y*tH*ww + x*tW + yy*ww + 15];
					outPixels[sy*w + yy*w + sx + tW + sh] = ip1;
				}
				for (int yy = 0; yy < tH; yy++) {
					for (int xx = 0; xx < tW; xx++) {
						int ip = inPixels[y*tH*ww + x*tW + yy*ww+xx];
						outPixels[sy*w + yy*w  + xx + sx + sh] = ip;
					}
				}
			}
		}
		return outPixels;
	}
	
	public static Texture createFromPNGFile(FilePath path, TextureFilter filter) throws IOException {
		return createFromBuffer(loadBufferOfPNG(path), filter);
	}
	
	public static PaddedTexture createFromPNGFile(FilePath path, TextureFilter filter, int padWidth, int padHeight) throws IOException {
		return createFromBuffer(loadBufferOfPNG(path), filter, padWidth, padHeight);
	}
	
	public static PaddedTexture createFromBuffer(Buffer buffer, TextureFilter filter, int padWidth, int padHeight) {
		int[] paddedPixels = padPixels(buffer, padWidth, padHeight);
		
		int nXTiles = buffer.getWidth()/padWidth;
		int nYTiles = buffer.getHeight()/padHeight;
		int oWidth = buffer.getWidth() + nXTiles*2;
		int oHeight = buffer.getHeight() + nYTiles*2;
		int w = scaleTo2N(oWidth);
		int h = scaleTo2N(oHeight);
		
		Buffer padBuffer = new Buffer(w, h, paddedPixels);
		Texture texture = createFromBuffer(padBuffer, filter);
		
		PaddedTexture paddedTexture = new PaddedTexture(
				texture.glId, w, h, filter);
		paddedTexture.padWidth = padWidth;
		paddedTexture.padHeight = padHeight;
		paddedTexture.originalWidth = oWidth;
		paddedTexture.originalHeight = oHeight;
		
		return paddedTexture;
	}
	
	public static Texture createFromBuffer(Buffer buffer, TextureFilter filter) {
		
		int glId = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, glId);	
		
		glTexImage2D(GL_TEXTURE_2D,
			     0,
			     GL_RGBA,
			     buffer.getWidth(),
			     buffer.getHeight(),
			     0,
			     GL_RGBA,
			     GL_UNSIGNED_BYTE,
			     buffer.asByteBuffer());
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
   		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter.getMinFilter());
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter.getMagFilter());
		
		glBindTexture(GL_TEXTURE_2D, 0);
		
		return new Texture(glId, buffer.getWidth(), buffer.getHeight(), filter);
	}
	
	public static Buffer loadBufferOfPNG(FilePath path) throws IOException {
		try (InputStream inStream = path.getInputStream()) {
					
			Buffer buffer = new Buffer();
			BufferedImage bufferedImage = ImageIO.read(inStream);
			
			buffer.width = bufferedImage.getWidth();
			buffer.height = bufferedImage.getHeight();
			
			buffer.pixels = bufferedImage.getRGB(0,
                    0,
                    buffer.width,
                    buffer.height,
                    null,
                    0,
                    bufferedImage.getWidth());
						
			return buffer;
			
		} catch (IOException e) {
			throw new IOException("Failed to load PNG file: " + path);
		} catch (IllegalArgumentException e) {
			throw new IOException("PNG file: " + path + " does not exist");
		}
	}
	
	public void bind() {
		glBindTexture(GL_TEXTURE_2D, glId);
	}
	
	@Override
	public void dispose() {
		glDeleteTextures(glId);
	}
}
