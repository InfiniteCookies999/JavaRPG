package client.game.entity;

import java.util.Random;

import vork.App;
import vork.gfx.Color;
import vork.gfx.SpriteBatch;
import vork.gfx.Texture;
import vork.gfx.UvCoords;

public class LevelUpRenderer {

	public float startX, startY;
	private int tick = 0;
	
	private class Particle {
		private float offsetX, offsetY;
		private float velocityX, velocityY;
		private Color color;
	}
	
	private static final Random random = new Random();
	
	private static final int NUM_PARTICLES = 10;
	private Particle[] particles = new Particle[NUM_PARTICLES];
	
	private Texture texture;
	
	public LevelUpRenderer() {
		
		texture = App.gfx.emptyTexture;
		
		for (int i = 0; i < NUM_PARTICLES; i++) {
			Particle particle = new Particle();
			particle.offsetX = random.nextFloat() * 2.5f;
			particle.offsetY = random.nextFloat() * 2.5f;
			
			float alpha = (float) Math.toRadians( random.nextFloat() * 60.0f + 60.0f );
			float scale = (random.nextFloat() + 26.5f) / 60.0f;
			
			particle.velocityX = (float) (scale * Math.cos(alpha));
			particle.velocityY = (float) ((scale+0.3f) * Math.sin(alpha));
			int primaryColor = random.nextInt(3);
			float colorDist = (int) (random.nextFloat() * 0.8f + 0.2f);
			float r = 0, g = 0, b = 0;
			switch (primaryColor) {
			case 0: r = 1; g = colorDist; b = 1 - g; break;
			case 1: g = 1; r = colorDist; b = 1 - r; break;
			case 2: b = 1; r = colorDist; g = 1 - r; break;
			}
			
			particle.color = new Color(r, g, b, 1);
			
			particles[i] = particle;
		}
	}
	
	public void render(SpriteBatch batch) {
	
		if (tick != 0) {
			final float gravityVelocity = 9.4f * tick / 300.0f;
			for (int i = 0; i < NUM_PARTICLES; i++) {
				particles[i].offsetX += particles[i].velocityX;
				particles[i].offsetY += particles[i].velocityY;
				particles[i].offsetY -= gravityVelocity;
			}
		}
		
		for (int i = 0; i < NUM_PARTICLES; i++) {
			
			batch.addQuad(
					startX + particles[i].offsetX,
					startY + particles[i].offsetY,
					2.0f,
					2.0f,
					particles[i].color,
					UvCoords.FULL,
					texture);
		}
		
		++tick;
	}
	
	public boolean isFinished() {
		return tick > 30;
	}
}
