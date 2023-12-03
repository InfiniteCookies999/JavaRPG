package client.game.entity;

import vork.gfx.Camera;
import vork.gfx.Color;
import vork.gfx.SpriteBatch;
import vork.gfx.VkFont;
import vork.math.Matrix4;
import vork.math.Vector2;

public class EntityHealthNumberRenderer {
	
	public float healthStartX, healthStartY;
	public int changeInHealth;
	private int tick = 0;
	
	private static final Color OUTLINE_COLOR = Color.rgb(0x1f1c1c);
	
	public void render(Camera worldCamera, Camera textCamera, SpriteBatch batch, VkFont font) {
		String num = String.valueOf(Math.abs(changeInHealth));
		
		Matrix4 projMat = worldCamera.getProjMat();
		Vector2 screenCoords = projMat.mul(new Vector2(healthStartX, healthStartY));
		Matrix4 invertedTextMat = textCamera.getProjMat().invertOrtho2D();
		
		Vector2 textWorldPos = invertedTextMat.mul(screenCoords);
		int xx = (int) (textWorldPos.x - (font.getLengthOfText(num) / textCamera.zoom + 2) / 2.0f);
		int yy = (int) textWorldPos.y;
		
		Color primaryColor = Color.WHITE;
		if (changeInHealth == 0) {
			primaryColor = Color.rgb(0x5e9cff);
		} else if (changeInHealth > 0) {
			primaryColor = Color.GREEN;
		}
		
		int v = Math.abs(changeInHealth);
		// 32767 -> 5
		int[] digitBuf = new int[5];
		int nDigits = 0;
		do {
			digitBuf[nDigits++] = v % 10;
			v /= 10;
		} while (v != 0);
	
		int digitCount = 0;
		int xx0 = xx, yy0 = yy;
		int xscan = 0;
		for (int di = nDigits-1; di >= 0; di--) {
			xx = xx0;
			yy = yy0;
		
			int dur = 15;
			int pt = (tick + digitCount*(dur/2)) % (dur*2);
			if (pt <= dur) {
				yy += pt/3;
			} else {
				yy += dur/2 - pt/3;
			}
			xx += xscan;
			
			int digit = digitBuf[di];
			
			String disDigit = String.valueOf(digit);
			
			font.render(xx-1, yy, disDigit, OUTLINE_COLOR);
			font.render(xx+1, yy, disDigit, OUTLINE_COLOR);
			font.render(xx, yy+1, disDigit, OUTLINE_COLOR);
			font.render(xx, yy-1, disDigit, OUTLINE_COLOR);
			
			font.render(xx, yy, disDigit, primaryColor);
			
			xscan += font.getLengthOfText(disDigit) / textCamera.zoom + 3;
			
			++digitCount;
		}
		
		++tick;
	}
	
	public boolean isFinished() {
		return tick > 30;
	}
}
