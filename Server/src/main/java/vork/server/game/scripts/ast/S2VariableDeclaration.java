package vork.server.game.scripts.ast;

import lombok.Getter;
import lombok.Setter;

@Getter
public class S2VariableDeclaration extends S2Node {

	private String name;
	@Setter
	private S2Node assignment;
	@Setter
	private int stackIndex;
	
	public S2VariableDeclaration(String name, S2Node assignment) {
		super(S2NodeKind.VARIABLE_DECLARATION);
		this.name = name;
		this.assignment = assignment;
	}
}
