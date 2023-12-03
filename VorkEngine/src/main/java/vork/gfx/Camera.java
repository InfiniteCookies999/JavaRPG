package vork.gfx;

import lombok.Getter;
import lombok.Setter;
import vork.App;
import vork.math.Matrix4;
import vork.math.Vector2;
import vork.util.Collision;

public class Camera {

	@Getter
	private Matrix4 projMat = Matrix4.identity();
	
	@Setter
	private boolean shouldCenter = false;
	
	@Getter
	private float centerX, centerY;
	
	public float zoom = 1.0f;
	
	public void centerAround(float x, float y) {
		centerX = x;
		centerY = y;
		shouldCenter = true;
	}
	
	public void update() {
		
		float wndWidth  = App.gfx.getWndWidth();
		float wndHeight = App.gfx.getWndHeight();
		
		float tx = -centerX + wndWidth*0.5f;
		float ty = -centerY + wndHeight*0.5f;
		
		projMat = Matrix4.ortho2D(0, wndWidth, 0, wndHeight);
	
 		if (shouldCenter) {
			projMat = projMat.translate(tx, ty, 0);

			projMat = projMat.translate(-tx + wndWidth*0.5f, -ty + wndHeight*0.5f, 0);
			projMat = projMat.scale(zoom, zoom);
			projMat = projMat.translate(+tx - wndWidth*0.5f, +ty - wndHeight*0.5f, 0);
		} else {
			projMat = projMat.scale(zoom, zoom);
		}
	}
	
	public boolean isInView(float x, float y, float width, float height) {
		float wndWidth  = App.gfx.getWndWidth();
		float wndHeight = App.gfx.getWndHeight();
		float tx = centerX - (wndWidth/zoom) * 0.5f;
		float ty = centerY - (wndHeight/zoom) * 0.5f;
		float pad = 10.0f;
		return Collision.rectToRect(x, y, width, height,
				tx-pad, ty-pad,
				wndWidth/zoom + pad*2, wndHeight/zoom + pad*2);
	}
	
	public Vector2 getCursorInWorldCoordinates() {
		int wndWidth  = App.gfx.getWndWidth();
		int wndHeight = App.gfx.getWndHeight();
		int curX = App.input.getCursorWndX();
		int curY = App.input.getCursorWndY();
		float nx = ((2.0f * curX) / wndWidth ) - 1.0f;
		float ny = ((2.0f * curY) / wndHeight) - 1.0f;
		return projMat.invertOrtho2D().mul(new Vector2(nx, ny));
	}
}
