package vork.server.game.scripts.ast;

import lombok.Getter;
import lombok.Setter;
import vork.server.game.scripts.S2Label;

@Getter
public class S2Identifier extends S2Node {
	private String name;
	private boolean refersToLabel;
	
	@Setter
	private S2Label refLabel;
	@Setter
	private S2VariableDeclaration refVariable;
	
	public S2Identifier(String name, boolean refersToLabel) {
		super(S2NodeKind.IDENTIFIER);
		this.name = name;
		this.refersToLabel = refersToLabel;
	}
}
