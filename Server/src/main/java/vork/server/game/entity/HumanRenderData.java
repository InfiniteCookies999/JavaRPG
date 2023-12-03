package vork.server.game.entity;

import vork.server.Constants;

public class HumanRenderData extends EntityRenderData {

	public int skinColor;
	public int eyeColor;
	public int hairStyle;
	public int hairColor;
	public int helmType;
	public int bodyType;
	public int legsType;
	
	public HumanRenderData() {
		super(EntityRenderType.HUMAN);
	}
	
	public static int getHumanPantsType(int style, int color) {
		return style * Constants.NUM_HUMAN_CLOTHES_COLORS + color;
	}
	
	public static int getHumanShirtType(int style, int color) {
		// Shirt indexes exist after the pants indexes
		return (Constants.NUM_HUMAN_PANTS_STYLES * Constants.NUM_HUMAN_CLOTHES_COLORS) +
				style * Constants.NUM_HUMAN_CLOTHES_COLORS + color;
	}
}
