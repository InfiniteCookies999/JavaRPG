package client.game.entity;

import client.Constants;
import client.game.Resources;
import vork.gfx.Color;
import vork.gfx.SpriteBatch;
import vork.gfx.Texture;
import vork.gfx.TextureFilter;
import vork.gfx.UvCoords;

public class HumanRenderer extends EntityRenderer {

	private Texture texture;
	private int tick;
	private int moveDrawIdx = 0;
	
	public int skinColor;
	public int eyeColor;
	
	public int hairColor;
	public int hairStyle;
	public int helmType;
	
	public int bodyType;
	public int legsType;
	
	public static int START_INDEXES_FOR_ARMOR = 0;
	
	public static int getHumanPantsType(int style, int color) {
		return style * Resources.NUM_HUMAN_CLOTHES_COLORS + color;
	}
	
	public static int getHumanShirtType(int style, int color) {
		// Shirt indexes exist after the pants indexes
		return (Resources.NUM_HUMAN_PANTS_STYLES * Resources.NUM_HUMAN_CLOTHES_COLORS) +
				style * Resources.NUM_HUMAN_CLOTHES_COLORS + color;
	}
	
	private int normalizeArmorType(int armorIndex) {
		if (armorIndex < Constants.ARMOR_IDS_OFFSET) {
			return armorIndex;
		}
		armorIndex -= Constants.ARMOR_IDS_OFFSET;
		armorIndex += START_INDEXES_FOR_ARMOR;
		return armorIndex;
	}
	
	@Override
	public void setup() {
		
		int[] skinPixels = Resources.getHumanTexturePixels(Resources.TXR_HUMAN_SKIN_IDXS[skinColor]);
		int[] pixels = new int[skinPixels.length];
		System.arraycopy(skinPixels, 0, pixels, 0, skinPixels.length);
		
		for (int index : Resources.HUMAN_EYE_BUFFER_INDEXES) {
			pixels[index] = Constants.HUMAN_EYE_COLORS[eyeColor];
		}
		
		if (helmType == 0) {
			int[] hairPixels = Resources.getHumanTexturePixels(Resources.TXR_HUMAN_HAIR_IDXS[hairStyle][hairColor]);
			joinPixels(pixels, hairPixels);	
		} else {
			int[] helmPixels = Resources.getHumanTexturePixels(normalizeArmorType(helmType));
			joinPixels(pixels, helmPixels);	
		}
		
		
		int[] bodyPixels = Resources.getHumanTexturePixels(normalizeArmorType(bodyType));
		joinPixels(pixels, bodyPixels);
		int[] pantsPixels = Resources.getHumanTexturePixels(normalizeArmorType(legsType));
		joinPixels(pixels, pantsPixels);
		
		Texture.Buffer buffer = new Texture.Buffer(48, 128, pixels);
		texture = Texture.createFromBuffer(buffer, TextureFilter.NEAREST_NEAREST);
	}
	
	public void reset() {
		dispose();
		setup();
	}
	
	private void joinPixels(int[] dest, int[] src) {
		for (int i = 0; i < src.length; i++) {
			int a = (src[i]>>24) & 0xFF;
			if (a != 0) { // TODO: properly blend
				dest[i] = src[i];
			}
		}
	}
	int t = 0;
	@Override
	public void render(SpriteBatch batch, Entity entity) {
		
		if (!entity.isMoving()) {
			tick = 0;
			moveDrawIdx = 0;
		}
		
		int yIdx = 0;
		switch (entity.getFacingDirection()) {
		case NORTH: yIdx = 0; break;
		case EAST:  yIdx = 1; break;
		case SOUTH: yIdx = 3; break;
		case WEST:  yIdx = 2; break;
		case NONE: yIdx = 0; break;
		}
		
		
		int tickSpeed = 28;
		tickSpeed = (int) (tickSpeed * 2.0 / 3.0);
		int tickRate = (int) (tickSpeed / Math.sqrt(entity.getMoveSpeed()));
		
		if (tick % tickRate == 0 && entity.isMoving()) {
			++moveDrawIdx;
		}
		
		int outDrawIdx = moveDrawIdx;
		if (entity.getFacingDirection() == Direction.EAST) {
			outDrawIdx = 2 - outDrawIdx;
		}
		
		++tick;
		
		batch.addQuad(
				entity.x,
				entity.y,
				16,
				32,
				Color.WHITE,
				UvCoords.createFromPixelSize(outDrawIdx*16, yIdx*32, 16, 32, 48, 128),
				texture,
				Resources.getShaderProgram(shaderProgramType)
				);
		uploadShaderProgramInfo(batch, entity);
	}

	@Override
	public int getEntityWidth() {
		return 16;
	}

	@Override
	public int getEntityHeight() {
		return 32;
	}

	@Override
	public float getRenderYOffset() {
		return 3;
	}
	
	@Override
	public void dispose() {
		texture.dispose();
	}
}
