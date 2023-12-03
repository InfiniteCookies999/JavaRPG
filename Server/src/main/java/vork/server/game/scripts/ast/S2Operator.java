package vork.server.game.scripts.ast;

import java.util.HashMap;
import java.util.Map;

import vork.server.game.scripts.S2TokenKind;

public enum S2Operator {
	ASSIGN,
	MULTIPLY,
	DIVIDE,
	MODULUS,
	PLUS,
	MINUS,
	PAIR;
	
	private static final Map<S2TokenKind, S2Operator> CONVERSIONS = new HashMap<>();
	
	static {
		CONVERSIONS.put(S2TokenKind.EQUAL, ASSIGN);
		CONVERSIONS.put(S2TokenKind.STAR, MULTIPLY);
		CONVERSIONS.put(S2TokenKind.SLASH, DIVIDE);
		CONVERSIONS.put(S2TokenKind.PERCENT, MODULUS);
		CONVERSIONS.put(S2TokenKind.PLUS, PLUS);
		CONVERSIONS.put(S2TokenKind.MINUS, MINUS);
		CONVERSIONS.put(S2TokenKind.AT, PAIR);
	}
	
	public static S2Operator fromTokenKind(S2TokenKind kind) {
		return CONVERSIONS.get(kind);
	}
}
