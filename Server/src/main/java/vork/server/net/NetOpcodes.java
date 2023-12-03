package vork.server.net;

public class NetOpcodes {
	// Shared
	public static final byte LOGIN                   = (byte) 0xFF;
	public static final byte CHAT_TEXT               = (byte) 0xFE;
	public static final byte CHANGE_FACING_DIRECTION = (byte) 0xFD;
	public static final byte CONTAINER_MOVE          = (byte) 0xFC;
	public static final byte PICKUP_ITEM             = (byte) 0xFB;
	public static final byte NPC_CHAT                = (byte) 0xFA;
	
	// Server -> Client
	public static final byte ENTITY_SPAWN        = 0x01;
	public static final byte ENTITY_DESPAWN      = 0x02;
	public static final byte ENTITY_MOVE         = 0x03;
	public static final byte HEALTH_CHANGE       = 0x04;
	public static final byte ENTITY_TELEPORT     = 0x05;
	public static final byte SET_XP              = 0x06;
	public static final byte ANIMATION           = 0x07;
	public static final byte SET_ITEM            = 0x08;
	public static final byte SPAWN_GROUND_ITEM   = 0x09;
	public static final byte DESPAWN_GROUND_ITEM = 0x0A;
	public static final byte INVENTORY_CONFIRM   = 0x0B;
	public static final byte OPEN_SHOP           = 0x0C;
	
	// Client -> Server
	public static final byte CHARACTER_CREATE     = 0x01;
	public static final byte MOVE_REQUEST         = 0x02;
	public static final byte ENTITY_SELECT_OPTION = 0x03;
	public static final byte DEATH_CONFIRMATION   = 0x04;
	public static final byte DROP_ITEM            = 0x06;
}
