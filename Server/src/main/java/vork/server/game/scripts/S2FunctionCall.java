package vork.server.game.scripts;

import java.util.List;

import lombok.Getter;
import vork.server.game.scripts.ast.S2Node;
import vork.server.game.scripts.ast.S2NodeKind;

@Getter
public class S2FunctionCall extends S2Node {

	private String name;
	private List<S2Node> arguments;
	
	public S2FunctionCall(String name, List<S2Node> arguments) {
		super(S2NodeKind.FUNCTION_CALL);
		this.name = name;
		this.arguments = arguments;
	}
}
