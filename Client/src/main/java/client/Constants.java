package client;

public class Constants {
	
	public static final String SERVER_ADDRESS = "localhost";
	
	// These are "world" pixels. Not pixels displayed by the monitor.
	public static final int WORLD_PIXEL_SCALE  = 3;
	public static final int NUM_VISIBLE_PIXELS = 12*20;
	
	public static final int MAX_USERNAME_LENGTH = 20;
	public static final String USERNAME_REGEX_PATTERN = "([a-z_A-Z])*";
	
	public static final int TILE_SIZE  = 16;
	public static final int CHUNK_SIZE = 16;
	public static final int CHUNK_SIZE_LOG2 = (int) (Math.log(CHUNK_SIZE) / Math.log(2));

	public static final int MAX_CHAT_TEXT_LENGTH = 120;
	public static final int MAX_CHAT_MESSAGES    = 30;
	public static final byte CHAT_COLOR_CODE     = (byte) 0xFF;
	
	public static final int INVENTORY_NUM_COLUMNS = 4;
	public static final int INVENTORY_NUM_ROWS = 5;
	
	public static final int PATHFIND_RADIUS = 15;
	
	public static final int[][] HUMAN_HAIR_COLORS = {
			{ 0xFFc4bda9, 0xFF6b5045, 0xFF40302b }, // Brown
			{ 0xFFffffde, 0xFFc2c2c2, 0xFF737373 }, // Grayish-white
			{ 0xFFffffde, 0xFFdbcc58, 0xFF6e5831 }, // Blonde
			{ 0xFFffffde, 0xFF6b9299, 0xFF3b565e }, // Blue
			{ 0xFFc4bda9, 0xFF9c8770, 0xFF5c5245 }, // Light-Brown
			{ 0xFF9a9a9a, 0xFF2a3036, 0xFF16171a }, // Black
			{ 0xFFffffde, 0xFF93c489, 0xFF308020 }, // Green
	};
	
	public static final int[][] HUMAN_CLOTHES_COLORS = {
			{ 0xFF0a32fa, 0xFF233cb8, 0xFF051fb0 }, // Blue
			{ 0xFF9f0b00, 0xFF800800, 0xFF5a0000 }, // Red
			{ 0xFF4a4545, 0xFF423c3c, 0xFF261f1f }, // Black
			{ 0xFF6bb06a, 0xFF498750, 0xFF28633f }, // Green
			{ 0xFF9c7f5f, 0xFF7a5e43, 0xFF5c412a }, // Brown
			{ 0xFF7a6651, 0xFF635141, 0xFF403023 }, // Dark Brown
			{ 0xFF885f9c, 0xFF714e78, 0xFF5c275e }, // Purple
			{ 0xFF8d9299, 0xFF70767d, 0xFF5a5f66 }, // Gray
			{ 0xFFfff1ad, 0xFFcfbf57, 0xFFa29100 }, // Yellow
	};
	
	public static final int[] HUMAN_EYE_COLORS = {
			0xFF2d52ba,
			0xFF3f7548,
			0xFF694218,
			0xFF583f75,
			0xFF950707
	};
	
	public static final int ARMOR_IDS_OFFSET = 400;
	
}
