package vork.server.game.scripts;

import java.util.HashSet;
import java.util.Set;

public enum S2TokenKind {
	
	EOF,
	UNKNOWN,
	IDENTIFIER,
	INT_LITERAL,
	STRING_LITERAL,
	
	LET,
	IF,
	ELSE,
	MATCH,
	
	EQUAL("="),
	STAR("*"),
	SLASH("/"),
	PERCENT("%"),
	PLUS("+"),
	MINUS("-"),
	
	OPEN_PAREN("("),
	CLOSE_PAREN(")"),
	COMMA(","),
	POUND("#"),
	COLON(":"),
	AT("@");
	
	private static final S2TokenKind KW_START = LET;
	private static final S2TokenKind KW_END   = MATCH;
	private static final Set<String> KEYWORDS = new HashSet<>();
	
	private String displayString;
	
	private S2TokenKind(String displayString) {
		this.displayString = displayString;
	}
	
	private S2TokenKind() {
		
	}
	
	static {
		S2TokenKind[] values = S2TokenKind.values();
		for (int i = KW_START.ordinal(); i <= KW_END.ordinal(); i++) {
			KEYWORDS.add(values[i].toString().toLowerCase());
		}
	}
	
	public static boolean isKeyword(String lexeme) {
		return KEYWORDS.contains(lexeme);
	}
	
	@Override
	public String toString() {
		if (displayString != null) {
			return displayString;
		} else {
			return super.toString().toLowerCase();
		}
	}
}
