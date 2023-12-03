package vork.server.game.scripts.ast;

import lombok.Getter;

@Getter
public class S2Node {
	private S2NodeKind kind;

	public S2Node(S2NodeKind kind) {
		this.kind = kind;
	}
}
