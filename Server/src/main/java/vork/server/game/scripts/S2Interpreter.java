package vork.server.game.scripts;

import java.util.List;

import lombok.AllArgsConstructor;
import vork.server.Server;
import vork.server.game.Shop;
import vork.server.game.entity.Npc;
import vork.server.game.entity.Player;
import vork.server.game.scripts.ast.S2BinaryOp;
import vork.server.game.scripts.ast.S2Identifier;
import vork.server.game.scripts.ast.S2Integer;
import vork.server.game.scripts.ast.S2Node;
import vork.server.game.scripts.ast.S2String;
import vork.server.game.scripts.ast.S2VariableDeclaration;
import vork.server.net.out.NpcChatPacketOut;
import vork.server.net.out.OpenShopPacketOut;

public class S2Interpreter {

	private S2Script script;
	
	private Npc npc;
	private Player player;
	
	// Current statements being processed
	private List<S2Node> scopeStatements;
	private int statementPointer = 0;
	
	private int currentNumberOfOptions = 0;
	private S2Label[] options = new S2Label[32]; // allow up to 32 options.
	private boolean needsUserInput = false;
	
	private Object[] stack = new Object[50]; // allow up to 50 variables to be stored on the stack.
	
	@AllArgsConstructor
	private class Pair {
		private Object LHS;
		private Object RHS;
	}
	
	public S2Interpreter(Npc npc, Player player, S2Script script) {
		this.script = script;
		scopeStatements = this.script.getStatements();
		this.npc = npc;
		this.player = player;
	}
	
	public boolean run(int option) {
		if (currentNumberOfOptions != 0) {
			if (option > currentNumberOfOptions) {
				// End the script because the client requested
				// an option not even listed!
				return false;
			}
			
			S2Label label = options[option];
			statementPointer = label.getStatementIndex();
			currentNumberOfOptions = 0;
		}
		
		while (statementPointer < scopeStatements.size()) {
			
			S2Node statement = scopeStatements.get(statementPointer);
			switch (statement.getKind()) {
			case FUNCTION_CALL:
				interpretFunctionCall((S2FunctionCall) statement);
				break;
			case VARIABLE_DECLARATION:
				interpretVariableDeclaration((S2VariableDeclaration) statement);
				break;
			case LABEL: // Can be ignored
				break;
			default:
				break;
			}
			
			++statementPointer;
			
			if (needsUserInput) {
				needsUserInput = false;
				return true;
			}
		}
		
		return false;
	}
	
	private Object interpretFunctionCall(S2FunctionCall functionCall) {
		switch (functionCall.getName()) {
		case "say":
			say(functionCall.getArguments());
			return null;
		case "options":
			options(functionCall.getArguments());
			return null;
		case "shop":
			shop(functionCall.getArguments());
			return null;
		default:
			throw new IllegalStateException("unimplemented function for: " + functionCall.getName());
		}
	}
	
	private void interpretVariableDeclaration(S2VariableDeclaration variableDeclaration) {
		Object value = interpretExpr(variableDeclaration.getAssignment());
		stack[variableDeclaration.getStackIndex()] = value;
	}
	
	private Object interpretExpr(S2Node expr) {
		switch (expr.getKind()) {
		case BINARY:
			return interpretBinaryOp((S2BinaryOp) expr);
		case FUNCTION_CALL:
			return interpretFunctionCall((S2FunctionCall) expr);
		case IDENTIFIER:
			return interpretIdentifier((S2Identifier) expr);
		case INTEGER:
			return ((S2Integer) expr).getValue();
		case STRING:
			return ((S2String) expr).getValue();
		default:
			throw new IllegalStateException("Unimplmented interpretExpr()");
		}
	}

	private Object interpretBinaryOp(S2BinaryOp binaryOp) {
		Object LHS = interpretExpr(binaryOp.getLHS());
		Object RHS = interpretExpr(binaryOp.getRHS());
		
		switch (binaryOp.getOperator()) {
		case PLUS: {
			if (LHS instanceof String && RHS instanceof String) {
				return ((String) LHS) + ((String) RHS);
			} else if (LHS instanceof String && RHS instanceof Integer) {
				return ((String) LHS) + ((Integer) RHS);
			} else if (LHS instanceof Integer && RHS instanceof String) {
				return ((Integer) LHS) + ((String) RHS);
			} else if (LHS instanceof Integer && RHS instanceof Integer) {
				return ((Integer) LHS) + ((Integer) RHS);
			} else {
				throw new IllegalStateException("Not handled yet");	
			}
		}
		case MINUS: {
			if (LHS instanceof Integer && RHS instanceof Integer) {
				return ((Integer) LHS) - ((Integer) RHS);
			} else if (LHS instanceof Integer && RHS instanceof Float) {
				return ((Integer) LHS) - ((Float) RHS);
			} else if (LHS instanceof Float && RHS instanceof Integer) {
				return ((Float) LHS) - ((Integer) RHS);
			} else if (LHS instanceof Float && RHS instanceof Float) {
				return ((Float) LHS) - ((Float) RHS);
			} else {
				throw new IllegalStateException("Not handled yet");
			}
		}
		case MULTIPLY: {
			if (LHS instanceof Integer && RHS instanceof Integer) {
				return ((Integer) LHS) * ((Integer) RHS);
			} else if (LHS instanceof Integer && RHS instanceof Float) {
				return ((Integer) LHS) * ((Float) RHS);
			} else if (LHS instanceof Float && RHS instanceof Integer) {
				return ((Float) LHS) * ((Integer) RHS);
			} else if (LHS instanceof Float && RHS instanceof Float) {
				return ((Float) LHS) * ((Float) RHS);
			} else {
				throw new IllegalStateException("Not handled yet");
			}
		}
		case DIVIDE: {
			if (LHS instanceof Integer && RHS instanceof Integer) {
				return ((Integer) LHS) / ((Integer) RHS);
			} else if (LHS instanceof Integer && RHS instanceof Float) {
				return ((Integer) LHS) / ((Float) RHS);
			} else if (LHS instanceof Float && RHS instanceof Integer) {
				return ((Float) LHS) / ((Integer) RHS);
			} else if (LHS instanceof Float && RHS instanceof Float) {
				return ((Float) LHS) / ((Float) RHS);
			} else {
				throw new IllegalStateException("Not handled yet");
			}
		}
		case MODULUS: {
			return ((Integer) LHS) / ((Integer) RHS);
		}
		case PAIR:
			return new Pair(LHS, RHS);
		case ASSIGN:
			throw new IllegalStateException("Not handled yet");
		default:
			throw new IllegalStateException("Unimplmented interpretBinaryOp()");
		}
	}
	
	private Object interpretIdentifier(S2Identifier identifier) {
		if (identifier.isRefersToLabel()) {
			return identifier.getRefLabel();
		} else {
			S2VariableDeclaration variableDeclaration = identifier.getRefVariable();
			return stack[variableDeclaration.getStackIndex()];
		}
	}
	
	private void say(List<S2Node> arguments) {
		String message = (String) interpretExpr(arguments.get(0));
		NpcChatPacketOut.say(npc.id, message).send(player);
		needsUserInput = true;
	}
	
	private void options(List<S2Node> arguments) {
		String message = (String) interpretExpr(arguments.get(0));
		String[] optionMessages = new String[arguments.size() - 1];
		for (int i = 0; i < arguments.size() - 1; i++) {
			Pair pair = (Pair) interpretExpr(arguments.get(i+1));
			optionMessages[i] = (String) pair.LHS;
			options[i] = ((S2Label) pair.RHS);
		}
		currentNumberOfOptions = optionMessages.length;
		NpcChatPacketOut.options(npc.id, message, optionMessages).send(player);
		needsUserInput = true;
	}
	
	private void shop(List<S2Node> arguments) {
		int shopIndex = (Integer) interpretExpr(arguments.get(0));
		Shop shop = Server.instance.getShop(shopIndex);
		player.setShopId(shop.getId());
		new OpenShopPacketOut(shop).send(player);
	}
}
