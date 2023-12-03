package vork.server.game.scripts.ast;

import lombok.Getter;

@Getter
public class S2Integer extends S2Node {

	private int value;
	
	public S2Integer(int value) {
		super(S2NodeKind.INTEGER);
		this.value = value;
	}
}
