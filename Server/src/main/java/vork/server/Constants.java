package vork.server;

import vork.server.game.Location;

public class Constants {

	public static final int TPS = 20;
	
	public static final int NUM_HUMAN_SKIN_COLORS    = 4;
	public static final int NUM_HUMAN_HAIR_COLORS    = 7;
	public static final int NUM_HUMAN_CLOTHES_COLORS = 9;
	public static final int NUM_HUMAN_EYE_COLORS     = 5;
	
	public static final int NUM_HUMAN_HAIR_STYLES  = 8;
	public static final int NUM_HUMAN_SHIRT_STYLES = 4;
	public static final int NUM_HUMAN_PANTS_STYLES = 5;
	
	public static final int ARMOR_IDS_OFFSET = 400;
	
	public static final Location DEFAULT_SPAWN_LOCATION = new Location(0, 0);
	
	/**
	 * The maximum amount of players allowed to be connected to
	 * the server. */
	public static final int MAX_ALLOWED_CLIENTS = 1000;
	/**
	 * The maximum amount of players that have their joining
	 * the world processed each tick. */
	public static final int MAX_PLAYER_JOIN_PROCESS = 100;
	
	/**
	 * To conserve processing power it is necessary  not to
	 * make too many checks regarding spawnability of entities
	 * therefore this value reduces the amount of other players a
	 * player is allowed to see. */
	public static final int MAX_VISIBLE_PLAYERS = 100;
	
	public static final int TILE_SIZE  = 16;
	public static final int CHUNK_SIZE = 16;
	public static final int CHUNK_SIZE_LOG2 = (int) (Math.log(CHUNK_SIZE) / Math.log(2));
	
	public static final int WORLD_MIN_CHUNK_X = -20, WORLD_MIN_CHUNK_Y = -20;
	public static final int WORLD_MAX_CHUNK_X = +20, WORLD_MAX_CHUNK_Y = +20;
	
	public static final int MAX_MOVE_QUEUE_SIZE = 16;
	
	public static final int MAX_CHAT_TEXT_LENGTH = 120;
	public static final char CHAT_COLOR_CODE     = 0xFF;
	
	public static final int INVENTORY_NUM_COLUMNS = 4;
	public static final int INVENTORY_NUM_ROWS    = 5;
	
	public static final int MAX_GROUND_ITEMS = 2000;
	
	public static final int MAX_SKILL_LEVEL = 140;
	
}
