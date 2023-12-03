package vork.server.game.scripts;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import lombok.AllArgsConstructor;
import vork.server.FilePath;
import vork.server.game.scripts.ast.S2BinaryOp;
import vork.server.game.scripts.ast.S2Identifier;
import vork.server.game.scripts.ast.S2Integer;
import vork.server.game.scripts.ast.S2Node;
import vork.server.game.scripts.ast.S2NodeKind;
import vork.server.game.scripts.ast.S2Operator;
import vork.server.game.scripts.ast.S2String;
import vork.server.game.scripts.ast.S2VariableDeclaration;

public class S2Parser {

	private S2Lexer lexer;
	private S2Token currentToken;
	
	private S2Script script;
	private S2Logger logger;
	
	private static final Map<S2TokenKind, Integer> BINARY_OPS_PRECEDENCE = new HashMap<>();
	static {
		BINARY_OPS_PRECEDENCE.put(S2TokenKind.STAR,    10);
		BINARY_OPS_PRECEDENCE.put(S2TokenKind.SLASH,   10);
		BINARY_OPS_PRECEDENCE.put(S2TokenKind.PERCENT, 10);
		BINARY_OPS_PRECEDENCE.put(S2TokenKind.PLUS,  9);
		BINARY_OPS_PRECEDENCE.put(S2TokenKind.MINUS, 9);
		BINARY_OPS_PRECEDENCE.put(S2TokenKind.AT, 1);
		BINARY_OPS_PRECEDENCE.put(S2TokenKind.EQUAL, 0);
	}
	
	public S2Script parse(FilePath file) throws IOException {
		script = new S2Script(file);
		this.logger = script.getLogger();
		
		try (BufferedReader reader = file.getBufferedReader()) {
			lexer = new S2Lexer(reader, logger);
			nextToken(); // Prime the parser
		
			while (!currentToken.is(S2TokenKind.EOF)) {
				S2Node statement = parseStmt();
				if (statement.getKind() == S2NodeKind.LABEL) {
					S2Label label = (S2Label) statement;
					script.getLabels().put(label.getName(), label);
				}
				script.getStatements().add(statement);
			}
		}
		
		return script;
	}
	
	private S2Node parseStmt() throws IOException {
		switch (currentToken.getKind()) {
		case EOF:
			return null;
		case LET:
			return parseVariableDeclaration();
		case POUND:
			return parseLabel();
		default:
			return parseExpr();
		}
	}
	
	private S2Node parseLabel() throws IOException {
		nextToken(); // Consuming #
		String name = parseIdentifier("Expected identifier for label");
		
		S2Label label = new S2Label(script.getStatements().size(), name);
		match(S2TokenKind.COLON);
		return label;
	}

	private S2Node parseVariableDeclaration() throws IOException {
		nextToken(); // Consuming 'let' token

		String name = parseIdentifier("Expected identifier for variable declaration");
		match(S2TokenKind.EQUAL);
		
		S2Node assignment = parseExpr();
		
		return new S2VariableDeclaration(name, assignment);
	}

	@AllArgsConstructor
	private class DelayedOp {
		private S2Token token;
		private S2Node term;
	}
	
	private S2Node parseExpr() throws IOException {
		
		S2Node LHS = parseTerm();
		
		Stack<DelayedOp> delayStack = new Stack<>();
		
		S2Token op = currentToken;
		while (BINARY_OPS_PRECEDENCE.containsKey(op.getKind())) {
			int prec = BINARY_OPS_PRECEDENCE.get(op.getKind());
			nextToken(); // Consuming the operator token
			
			S2Node RHS = parseTerm();
			boolean moreOperators = BINARY_OPS_PRECEDENCE.containsKey(currentToken.getKind());
			if (moreOperators &&
				BINARY_OPS_PRECEDENCE.get(currentToken.getKind()) > prec
					) {
				// Delaying the operation until later since the next
				// operator has a higher precedence.
				delayStack.push(new DelayedOp(op, LHS));
				LHS = RHS;
				op = currentToken;
			} else {
				
				LHS = createBinaryOp(op, LHS, RHS);
				
				while (!delayStack.empty()) {
					RHS = LHS;
					DelayedOp delayed = delayStack.peek();
					// Still possible to have the right side have higher precedence.
					if (moreOperators &&
						BINARY_OPS_PRECEDENCE.get(currentToken.getKind()) > BINARY_OPS_PRECEDENCE.get(delayed.token.getKind())) {
						LHS = RHS;
						op = currentToken;
						break;
					}
					
					delayStack.pop();
					LHS = delayed.term;
					
					LHS = createBinaryOp(delayed.token, LHS, RHS);
				}
				op = currentToken;
			}
		}
		
		return LHS;
	}
	
	private S2Node createBinaryOp(S2Token opToken, S2Node LHS, S2Node RHS) {
		
		S2NodeKind LKind = LHS.getKind();
		S2NodeKind RKind = RHS.getKind();
		
		S2TokenKind op = opToken.getKind();
		
		if (op == S2TokenKind.PLUS) {
			if (LKind == S2NodeKind.INTEGER && RKind == S2NodeKind.INTEGER) {
				int value = ((S2Integer) LHS).getValue() +
						    ((S2Integer) RHS).getValue();
				return new S2Integer(value);
			} else if (LKind == S2NodeKind.STRING && RKind == S2NodeKind.STRING) {
				String value = ((S2String) LHS).getValue() +
					           ((S2String) RHS).getValue();
				return new S2String(value);
			} else if (LKind == S2NodeKind.STRING && RKind == S2NodeKind.INTEGER) {
				String value = ((S2String) LHS).getValue() +
				               ((S2Integer) RHS).getValue();
				return new S2String(value);
			} else if (LKind == S2NodeKind.INTEGER && RKind == S2NodeKind.STRING) {
				String value = ((S2Integer) LHS).getValue() +
			                   ((S2String) RHS).getValue();
				return new S2String(value);
			}	
		} else if (LKind == S2NodeKind.INTEGER && RKind == S2NodeKind.INTEGER) {
			switch (op) {
			case MINUS: {
				int value = ((S2Integer) LHS).getValue() -
					        ((S2Integer) RHS).getValue();
				return new S2Integer(value);
			}
			case STAR: {
				int value = ((S2Integer) LHS).getValue() *
					        ((S2Integer) RHS).getValue();
				return new S2Integer(value);
			}
			case SLASH: {
				int rValue = ((S2Integer) RHS).getValue();
				if (rValue == 0) {
					error("Division by zero");
				}
				
				int value = ((S2Integer) LHS).getValue() /
				            rValue;
				return new S2Integer(value);
			}
			case PERCENT: {
				int rValue = ((S2Integer) RHS).getValue();
				if (rValue == 0) {
					error("Division by zero");
				}
				
				int value = ((S2Integer) LHS).getValue() %
				            rValue;
				return new S2Integer(value);
			}
			default:
				break;
			}
		}
		
		return new S2BinaryOp(S2Operator.fromTokenKind(op), LHS, RHS);
	}
	
	private S2Node parseTerm() throws IOException {
		switch (currentToken.getKind()) {
		case IDENTIFIER: {
			String name = currentToken.getLexeme();
			nextToken(); // Consuming identifier token
			if (currentToken.is(S2TokenKind.OPEN_PAREN)) {
				return parseFunctionCall(name);
			} else {
				return new S2Identifier(name, false);	
			}
		}
		case POUND: {
			nextToken(); // Consuming '#' token
			String name = parseIdentifier("Expected identifier for label");
			return new S2Identifier(name, true);
		}
		case INT_LITERAL:
			return parseIntegerLiteral();
		case STRING_LITERAL:
			return parseStringLiteral();
		default:
			error("expected valid expression");
			skipRecovery();
			return null;
		}
	}

	private S2Integer parseIntegerLiteral() throws IOException {
		
		String lexeme = currentToken.getLexeme();
		nextToken(); // Consuming integer token
		
		int value = 0, prevValue = 0, index = 0;
		while (index < lexeme.length()) {
			char c = lexeme.charAt(index);
			++index;
			prevValue = value;
			value = value * 10 + (c - '0');
			if (value < prevValue) {
				error("Numeric overflow");
			}
		}
		
		return new S2Integer(value);
	}
	
	private S2String parseStringLiteral() throws IOException {
		
		String lexeme = currentToken.getLexeme();
		nextToken(); // Consuming string token
		
		String value = "";
		int index = 1;
		while (index < lexeme.length()) {
			char c = lexeme.charAt(index);
			++index;
			if (c == '\\') {
				c = lexeme.charAt(index);
				++index;
				switch (c) {
				case '\\': value += '\\'; break;
				case 'n':  value += '\n'; break;
				case 't':  value += '\t'; break;
				case '0':  value += '\0'; break;
				case '"':  value += '"';  break;
				default:
					error("Unexpected escape character: " + c);
					break;
				}
			} else {
				value += c;
			}
		}
		
		return new S2String(value);
	}
	
	private S2FunctionCall parseFunctionCall(String name) throws IOException {
		List<S2Node> arguments = new ArrayList<>();
		nextToken(); // Consuming '(' token
		if (currentToken.getKind() != S2TokenKind.CLOSE_PAREN) {
			boolean moreArguments = false;
			do {
				
				arguments.add(parseExpr());
				
				moreArguments = currentToken.is(S2TokenKind.COMMA);
				if (moreArguments) {
					nextToken(); // Consuming ',' token
				}
			} while (moreArguments);
		}
		
		match(S2TokenKind.CLOSE_PAREN);
		
		return new S2FunctionCall(name, arguments);
	}
	
	private void skipRecovery() throws IOException {
		int errorLineNumber = lexer.getLineNumber();
		currentToken = lexer.nextToken();
		// Eat until the end of the line.
		while (currentToken.getKind() != S2TokenKind.EOF &&
			   errorLineNumber == lexer.getLineNumber()) {
			currentToken = lexer.nextToken();
		}
	}

	private void nextToken() throws IOException {
		currentToken = lexer.nextToken();
		while (currentToken.getKind() == S2TokenKind.UNKNOWN) {
			currentToken = lexer.nextToken();
		}
	}
	
	private void match(S2TokenKind kind) throws IOException {
		if (!currentToken.is(kind)) {
			error("expected " + kind.toString() + " but found: " + currentToken.getKind());
		} else {
			nextToken();
		}
	}
	
	private String parseIdentifier(String reason) throws IOException {
		if (currentToken.is(S2TokenKind.IDENTIFIER)) {
			String identifier = currentToken.getLexeme();
			nextToken();
			return identifier;
		} else {
			error(reason);
			return "";
		}
	}
	
	private void error(String message) {
		logger.error(new S2Location(lexer.getLineNumber(), 0), message);
	}
}
