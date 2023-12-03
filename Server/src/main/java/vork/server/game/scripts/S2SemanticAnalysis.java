package vork.server.game.scripts;

import java.util.HashMap;
import java.util.Map;

import vork.server.game.scripts.ast.S2BinaryOp;
import vork.server.game.scripts.ast.S2Identifier;
import vork.server.game.scripts.ast.S2Node;
import vork.server.game.scripts.ast.S2VariableDeclaration;

public class S2SemanticAnalysis {
	
	private Map<String, S2VariableDeclaration> declarations = new HashMap<>();
	
	private S2Script script;
	private S2Logger logger;
	
	public void analyze(S2Script script) {
		this.script = script;
		this.logger = script.getLogger();
		script.getStatements().forEach(this::check);
	}
	
	private void check(S2Node node) {
		switch (node.getKind()) {
		case BINARY:
			checkBinaryOp((S2BinaryOp) node);
			break;
		case FUNCTION_CALL:
			checkFunctionCall((S2FunctionCall) node);
			break;
		case IDENTIFIER:
			checkIdentifier((S2Identifier) node);
			break;
		case INTEGER:
			break;
		case STRING:
			break;
		case VARIABLE_DECLARATION:
			checkVariableDeclaration((S2VariableDeclaration) node);
			break;
		default:
			break;
		}
	}

	private void checkBinaryOp(S2BinaryOp binaryOp) {
		check(binaryOp.getLHS());
		check(binaryOp.getRHS());
	}
	
	private void checkVariableDeclaration(S2VariableDeclaration variableDeclaration) {
		declarations.put(variableDeclaration.getName(), variableDeclaration);
		check(variableDeclaration.getAssignment());
	}
	
	private void checkIdentifier(S2Identifier identifier) {
		if (identifier.isRefersToLabel()) {
			S2Label label = script.getLabels().get(identifier.getName());
			if (label != null) {
				identifier.setRefLabel(label);
			} else {
				error("Unknown label: " + identifier.getName());
			}
		} else {
			S2VariableDeclaration variableDeclaration = declarations.get(identifier.getName());
			if (variableDeclaration != null) {
				identifier.setRefVariable(variableDeclaration);
			} else {
				error("Unknown variable: " + identifier.getName());
			}
		}
	}
	
	private void checkFunctionCall(S2FunctionCall functionCall) {
		for (S2Node argument : functionCall.getArguments()) {
			check(argument);
		}
	}
	
	private void error(String message) {
		logger.error(new S2Location(0, 0), message);
	}
}
