package vork.server.game.scripts.ast;

import vork.server.game.scripts.S2FunctionCall;
import vork.server.game.scripts.S2Script;

public class ASTPrinter {
	
	private static final int INDENT_WIDTH = 4;
	
	public void print(S2Script script) {
		for (S2Node statement : script.getStatements()) {
			print(statement, 0);
			System.out.println();
		}
	}
	
	private void print(S2Node node, int depth) {
		if (depth > 0) {
			System.out.print(" ".repeat(depth * INDENT_WIDTH));	
		}
		switch (node.getKind()) {
		case VARIABLE_DECLARATION:
			printVariableDeclaration((S2VariableDeclaration) node, depth);
			break;
		case BINARY:
			printBinaryOp((S2BinaryOp) node, depth);
			break;
		case IDENTIFIER:
			printIdentifier((S2Identifier) node, depth);
			break;
		case INTEGER:
			printInteger((S2Integer) node, depth);
			break;
		case STRING:
			printString((S2String) node, depth);
			break;
		case FUNCTION_CALL:
			printFunctionCall((S2FunctionCall) node, depth);
			break;
		default:
			break;
		}
	}

	private void printVariableDeclaration(S2VariableDeclaration variableDeclaration, int depth) {
		System.out.println("Variable Declaration {name=\"" + variableDeclaration.getName() + "\"}");
		print(variableDeclaration.getAssignment(), depth + 1);
	}

	private void printBinaryOp(S2BinaryOp binaryOp, int depth) {
		System.out.println(String.format("Binary Operator {op = %s}", binaryOp.getOperator()));
		print(binaryOp.getLHS(), depth + 1);
		System.out.println();
		print(binaryOp.getRHS(), depth + 1);
	}
	
	private void printIdentifier(S2Identifier identifier, int depth) {
		System.out.print("Identifier {name = \"" + identifier.getName() + "\"}");
	}
	
	private void printInteger(S2Integer integer, int depth) {
		System.out.print("Integer {value = " + integer.getValue() + "}");
	}
	
	private void printString(S2String string, int depth) {
		System.out.print("String {value = \"" + string.getValue() + "\"}");
	}
	
	private void printFunctionCall(S2FunctionCall functionCall, int depth) {
		System.out.println("Function call {name = \"" + functionCall.getName() +  "\"}");
		for (S2Node argument : functionCall.getArguments()) {
			print(argument, depth + 1);
			System.out.println();
		}
	}
}
