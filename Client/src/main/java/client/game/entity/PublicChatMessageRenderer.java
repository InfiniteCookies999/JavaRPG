package client.game.entity;

import client.game.menus.TextChatMenu;
import vork.gfx.Camera;
import vork.gfx.SpriteBatch;
import vork.gfx.VkFont;
import vork.math.Matrix4;
import vork.math.Vector2;

public class PublicChatMessageRenderer {
	public Entity entity;
	public String message;
	private int tick = 0;
	
	public void render(Camera worldCamera, Camera textCamera, SpriteBatch batch, VkFont font) {
		
		float x = entity.x + entity.renderer.getEntityWidth()/2;
		float y = entity.y + entity.renderer.getEntityHeight();
		
		Matrix4 projMat = worldCamera.getProjMat();
		Vector2 screenCoords = projMat.mul(new Vector2(x, y));
		Matrix4 invertedTextMat = textCamera.getProjMat().invertOrtho2D();
		
		Vector2 textWorldPos = invertedTextMat.mul(screenCoords);
		int xx = (int) (textWorldPos.x - (font.getLengthOfText(message) / textCamera.zoom + 2) / 2.0f);
		int yy = (int) textWorldPos.y;
		
		font.render(xx, yy, message, TextChatMenu.PUBLIC_CHAT_COLOR.adjustSaturation(+70));
		
		++tick;
	}
	
	public boolean isFinished() {
		return tick > 60*2.5;
	}
}
