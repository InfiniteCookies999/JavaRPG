package vork.server.game.scripts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class S2Token {
	private S2TokenKind kind;
	private String lexeme;

	public boolean is(S2TokenKind kind) {
		return this.kind == kind;
	}
}
