package vork.server.game.scripts;

import lombok.Getter;
import vork.server.game.scripts.ast.S2Node;
import vork.server.game.scripts.ast.S2NodeKind;

@Getter
public class S2Label extends S2Node {

	private int statementIndex = 0;
	private String name;
	
	public S2Label(int statementIndex, String name) {
		super(S2NodeKind.LABEL);
		this.statementIndex = statementIndex;
		this.name = name;
	}
}
