package vork.server.game.scripts.ast;

import lombok.Getter;

@Getter
public class S2String extends S2Node {
	
	private String value;
	
	public S2String(String value) {
		super(S2NodeKind.STRING);
		this.value = value;
	}
}
