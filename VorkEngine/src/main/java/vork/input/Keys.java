package vork.input;

import org.lwjgl.glfw.GLFW;

public class Keys {

	/**
	 * GLFW works with lower 9 bits anything
	 * above that range may be used.
	 */
	public static final int SHIFT_MASK = 0x200;
	public static final int CAPS_MASK  = 0x400;
	public static final int LAST_MASK  = CAPS_MASK;
	
	/**
	 * Used to not negate the modifier mask
	 * from the keys
	 */
	public static final int NO_SHIFT_MASK = 0x800;
	public static final int NO_CAPS_MASK  = 0x1000;
	
	public static final int A = GLFW.GLFW_KEY_A;
	public static final int B = GLFW.GLFW_KEY_B;
	public static final int C = GLFW.GLFW_KEY_C;
	public static final int D = GLFW.GLFW_KEY_D;
	public static final int E = GLFW.GLFW_KEY_E;
	public static final int F = GLFW.GLFW_KEY_F;
	public static final int G = GLFW.GLFW_KEY_G;
	public static final int H = GLFW.GLFW_KEY_H;
	public static final int I = GLFW.GLFW_KEY_I;
	public static final int J = GLFW.GLFW_KEY_J;
	public static final int K = GLFW.GLFW_KEY_K;
	public static final int L = GLFW.GLFW_KEY_L;
	public static final int M = GLFW.GLFW_KEY_M;
	public static final int N = GLFW.GLFW_KEY_N;
	public static final int O = GLFW.GLFW_KEY_O;
	public static final int P = GLFW.GLFW_KEY_P;
	public static final int Q = GLFW.GLFW_KEY_Q;
	public static final int R = GLFW.GLFW_KEY_R;
	public static final int S = GLFW.GLFW_KEY_S;
	public static final int T = GLFW.GLFW_KEY_T;
	public static final int U = GLFW.GLFW_KEY_U;
	public static final int V = GLFW.GLFW_KEY_V;
	public static final int W = GLFW.GLFW_KEY_W;
	public static final int X = GLFW.GLFW_KEY_X;
	public static final int Y = GLFW.GLFW_KEY_Y;
	public static final int Z = GLFW.GLFW_KEY_Z;
	
	public static final int NUM_0 = GLFW.GLFW_KEY_0;
	public static final int NUM_1 = GLFW.GLFW_KEY_1;
	public static final int NUM_2 = GLFW.GLFW_KEY_2;
	public static final int NUM_3 = GLFW.GLFW_KEY_3;
	public static final int NUM_4 = GLFW.GLFW_KEY_4;
	public static final int NUM_5 = GLFW.GLFW_KEY_5;
	public static final int NUM_6 = GLFW.GLFW_KEY_6;
	public static final int NUM_7 = GLFW.GLFW_KEY_7;
	public static final int NUM_8 = GLFW.GLFW_KEY_8;
	public static final int NUM_9 = GLFW.GLFW_KEY_9;
	
	public static final int NUM_NO_SHIFT_0 = GLFW.GLFW_KEY_0 | NO_SHIFT_MASK;
	public static final int NUM_NO_SHIFT_1 = GLFW.GLFW_KEY_1 | NO_SHIFT_MASK;
	public static final int NUM_NO_SHIFT_2 = GLFW.GLFW_KEY_2 | NO_SHIFT_MASK;
	public static final int NUM_NO_SHIFT_3 = GLFW.GLFW_KEY_3 | NO_SHIFT_MASK;
	public static final int NUM_NO_SHIFT_4 = GLFW.GLFW_KEY_4 | NO_SHIFT_MASK;
	public static final int NUM_NO_SHIFT_5 = GLFW.GLFW_KEY_5 | NO_SHIFT_MASK;
	public static final int NUM_NO_SHIFT_6 = GLFW.GLFW_KEY_6 | NO_SHIFT_MASK;
	public static final int NUM_NO_SHIFT_7 = GLFW.GLFW_KEY_7 | NO_SHIFT_MASK;
	public static final int NUM_NO_SHIFT_8 = GLFW.GLFW_KEY_8 | NO_SHIFT_MASK;
	public static final int NUM_NO_SHIFT_9 = GLFW.GLFW_KEY_9 | NO_SHIFT_MASK;
	
	public static final int EXCLAMATION       = NUM_1 | SHIFT_MASK;
	public static final int AROBASE           = NUM_2 | SHIFT_MASK;
	public static final int POUND             = NUM_3 | SHIFT_MASK;
	public static final int DOLLAR            = NUM_4 | SHIFT_MASK;
	public static final int PERCENT           = NUM_5 | SHIFT_MASK;
	public static final int CARET             = NUM_6 | SHIFT_MASK;
	public static final int AMPERSAND         = NUM_7 | SHIFT_MASK;
	public static final int MULTIPLY          = NUM_8 | SHIFT_MASK;
	public static final int OPEN_PARENTHESIS  = NUM_9 | SHIFT_MASK;
	public static final int CLOSE_PARENTHESIS = NUM_0 | SHIFT_MASK;
	
	public static final int F1  = GLFW.GLFW_KEY_F1;
	public static final int F2  = GLFW.GLFW_KEY_F2;
	public static final int F3  = GLFW.GLFW_KEY_F3;
	public static final int F4  = GLFW.GLFW_KEY_F4;
	public static final int F5  = GLFW.GLFW_KEY_F5;
	public static final int F6  = GLFW.GLFW_KEY_F6;
	public static final int F7  = GLFW.GLFW_KEY_F7;
	public static final int F8  = GLFW.GLFW_KEY_F8;
	public static final int F9  = GLFW.GLFW_KEY_F9;
	public static final int F10 = GLFW.GLFW_KEY_F10;
	public static final int F11 = GLFW.GLFW_KEY_F11;
	public static final int F12 = GLFW.GLFW_KEY_F12;

	public static final int UP    = GLFW.GLFW_KEY_UP;
	public static final int DOWN  = GLFW.GLFW_KEY_DOWN;
	public static final int LEFT  = GLFW.GLFW_KEY_LEFT;
	public static final int RIGHT = GLFW.GLFW_KEY_RIGHT;
	
	public static final int ESCAPE      = GLFW.GLFW_KEY_ESCAPE;
	public static final int MINUS       = GLFW.GLFW_KEY_MINUS;
	public static final int PLUS        = GLFW.GLFW_KEY_KP_ADD;
	public static final int BACKSPACE   = GLFW.GLFW_KEY_BACKSPACE;
	public static final int INSERT      = GLFW.GLFW_KEY_INSERT;
	public static final int HOME        = GLFW.GLFW_KEY_HOME;
	public static final int PAGE_UP     = GLFW.GLFW_KEY_PAGE_UP;
	public static final int DELETE      = GLFW.GLFW_KEY_DELETE;
	public static final int END         = GLFW.GLFW_KEY_END;
	public static final int PAGE_DOWN   = GLFW.GLFW_KEY_PAGE_DOWN;
	public static final int ENTER       = GLFW.GLFW_KEY_ENTER;
	public static final int LEFT_SHIFT  = GLFW.GLFW_KEY_LEFT_SHIFT;
	public static final int RIGHT_SHIFT = GLFW.GLFW_KEY_RIGHT_SHIFT;
	public static final int LEFT_CTRL   = GLFW.GLFW_KEY_LEFT_CONTROL;
	public static final int RIGHT_CTRL  = GLFW.GLFW_KEY_RIGHT_CONTROL;
	public static final int LEFT_ALT    = GLFW.GLFW_KEY_LEFT_ALT;
	public static final int RIGHT_ALT   = GLFW.GLFW_KEY_RIGHT_ALT;
	public static final int TAB         = GLFW.GLFW_KEY_TAB;
	
	public static final int NUM_LOCK = GLFW.GLFW_KEY_NUM_LOCK;
	public static final int NUMPAD_0 = GLFW.GLFW_KEY_KP_0;
	public static final int NUMPAD_1 = GLFW.GLFW_KEY_KP_1;
	public static final int NUMPAD_2 = GLFW.GLFW_KEY_KP_2;
	public static final int NUMPAD_3 = GLFW.GLFW_KEY_KP_3;
	public static final int NUMPAD_4 = GLFW.GLFW_KEY_KP_4;
	public static final int NUMPAD_5 = GLFW.GLFW_KEY_KP_5;
	public static final int NUMPAD_6 = GLFW.GLFW_KEY_KP_6;
	public static final int NUMPAD_7 = GLFW.GLFW_KEY_KP_7;
	public static final int NUMPAD_8 = GLFW.GLFW_KEY_KP_8;
	public static final int NUMPAD_9 = GLFW.GLFW_KEY_KP_9;
}
