package client.game.entity;

import client.game.ShaderProgramType;
import lombok.Setter;
import vork.Disposable;
import vork.gfx.SpriteBatch;
import vork.math.Vector2;

public abstract class EntityRenderer implements Disposable {

	@Setter
	protected ShaderProgramType shaderProgramType = ShaderProgramType.DEFAULT;
	
	@Setter
	private int presentationTick = 0;
	
	public void setup() {
		
	}
	
	protected void uploadShaderProgramInfo(SpriteBatch batch, Entity entity) {
		switch (shaderProgramType) {
		case DEFAULT_ATTACKED:
			batch.attachUniform1i("u_tick", presentationTick);
			batch.attachUniform1i("u_duration", 10);
			batch.attachUniform2f("u_originalPos", new Vector2(entity.x, entity.y));
			batch.attachUniform2f("u_entitySize", new Vector2(getEntityWidth(), getEntityHeight()));
			if (presentationTick++ > 30) {
				presentationTick = 0;
				shaderProgramType = ShaderProgramType.DEFAULT;
			}
			break;
		case ENTITY_DIED:
			batch.attachUniform1i("u_tick", presentationTick);
			batch.attachUniform2f("u_originalPos", new Vector2(entity.x, entity.y));
			batch.attachUniform2f("u_entitySize", new Vector2(getEntityWidth(), getEntityHeight()));
			++presentationTick;
			break;
		default:
			break;
		}
	}
	
	public abstract void render(SpriteBatch batch, Entity entity);
	
	public abstract int getEntityWidth();
	
	public abstract int getEntityHeight();
	
	public float getRenderYOffset() {
		return 0;
	}
	
}
