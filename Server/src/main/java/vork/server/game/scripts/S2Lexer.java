package vork.server.game.scripts;

import java.io.BufferedReader;
import java.io.IOException;

import lombok.Getter;

public class S2Lexer {

	private BufferedReader reader;
	@Getter
	private int lineNumber = 1;
	
	private int cur;
	private int[] backlog = new int[3];
	private int backlogCount = 0;
	
	public static final int EOF = -1;
	
	private S2Logger logger;
	
	public S2Lexer(BufferedReader reader, S2Logger logger) throws IOException {
		this.reader = reader;
		cur = reader.read();
		this.logger = logger;
	}

	public S2Token nextToken() throws IOException {
		
		skipIgnored();
		
		switch (cur) {
		case 'a': case 'b': case 'c': case 'd': case 'e':
		case 'f': case 'g': case 'h': case 'i': case 'j':
		case 'k': case 'l': case 'm': case 'n': case 'o':
		case 'p': case 'q': case 'r': case 's': case 't':
		case 'u': case 'v': case 'w': case 'x': case 'y':
		case 'z':
		case 'A': case 'B': case 'C': case 'D': case 'E':
		case 'F': case 'G': case 'H': case 'I': case 'J':
		case 'K': case 'L': case 'M': case 'N': case 'O':
		case 'P': case 'Q': case 'R': case 'S': case 'T':
		case 'U': case 'V': case 'W': case 'X': case 'Y':
		case 'Z':
			return nextWord();
		case '0': case '1': case '2': case '3': case '4':
		case '5': case '6': case '7': case '8': case '9':
			return nextNumber();
		case '"':
			return nextString();
		case '=':
			return andEat(new S2Token(S2TokenKind.EQUAL, "="));
		case '*':
			return andEat(new S2Token(S2TokenKind.STAR, "*"));
		case '/':
			return andEat(new S2Token(S2TokenKind.SLASH, "/"));
		case '%':
			return andEat(new S2Token(S2TokenKind.PERCENT, "%"));
		case '+':
			return andEat(new S2Token(S2TokenKind.PLUS, "+"));
		case '-':
			return andEat(new S2Token(S2TokenKind.MINUS, "-"));
		case '(':
			return andEat(new S2Token(S2TokenKind.OPEN_PAREN, "("));
		case ')':
			return andEat(new S2Token(S2TokenKind.CLOSE_PAREN, ")"));
		case ',':
			return andEat(new S2Token(S2TokenKind.COMMA, ","));
		case '#':
			return andEat(new S2Token(S2TokenKind.POUND, "#"));
		case ':':
			return andEat(new S2Token(S2TokenKind.COLON, ":"));
		case '@':
			return andEat(new S2Token(S2TokenKind.AT, "@"));
		case EOF:
			return new S2Token(S2TokenKind.EOF, "");
		default:
			error(0, "unknown character: " + (char) cur);
			next();
			return new S2Token(S2TokenKind.UNKNOWN, "");
		}
	}

	private S2Token andEat(S2Token token) throws IOException {
		next();
		return token;
	}

	private void skipIgnored() throws IOException {
		while (true) {
			switch (cur) {
			case ' ': case '\t': case '\f':
				next();
				break;
			case '\n':
				++lineNumber;
				next();
				break;
			case '\r':
				++lineNumber;
				next();
				if (cur == '\n') {
					next();
				}
				break;
			case '/':
				peek();
				if (cur == '/') {
					eatSingleLineComment();
					break;
				} else {
					return;
				}
			default:
				return;
			}
		}
		
	}
	
	private void eatSingleLineComment() throws IOException {
		while (true) {
			switch (cur) {
			case '\n': case '\r': case EOF:
				return;
			default:
				next();
				break;
			}
		}
	}

	private S2Token nextWord() throws IOException {
		String lexeme = String.valueOf((char) cur);
		
		next();
		while (Character.isAlphabetic(cur)) {
			lexeme += (char) cur;
			next();
		}
		
		if (S2TokenKind.isKeyword(lexeme)) {
			return new S2Token(S2TokenKind.valueOf(lexeme.toUpperCase()), lexeme);
		}
		
		return new S2Token(S2TokenKind.IDENTIFIER, lexeme);
	}
	
	private S2Token nextNumber() throws IOException {
		String lexeme = String.valueOf((char) cur);
		
		next();
		while (Character.isDigit(cur)) {
			lexeme += (char) cur;
			next();
		}
		
		return new S2Token(S2TokenKind.INT_LITERAL, lexeme);
	}
	
	private S2Token nextString() throws IOException {
		String lexeme = "\"";
		next();
		
		boolean keepGoing = true;
		while (keepGoing) {
			switch (cur) {
			case '"':
				keepGoing = false;
				break;
			case '\\':
				next();
				if (cur == '"')
					next();
				break;
			default:
				lexeme += (char) cur;
				next();
				break;
			}
		}
		
		if (cur != '"') {
			error(0, "Expected closing quotation");
		} else {
			next();
		}
		
		return new S2Token(S2TokenKind.STRING_LITERAL, lexeme);
	}
	
	private void next() throws IOException {
		if (backlogCount != 0) {
			cur = backlog[backlogCount - 1];
			--backlogCount;
		} else {
			cur = reader.read();
		}
	}
	
	private void peek() throws IOException {
		backlog[backlogCount] = cur;
		++backlogCount;
		cur = reader.read();
	}
	
	private void error(int fileIndex, String message) {
		logger.error(new S2Location(lineNumber, fileIndex), message);
	}
}
