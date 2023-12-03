package vork.server.game.scripts.ast;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2BinaryOp extends S2Node {
	private S2Operator operator;
	private S2Node LHS;
	private S2Node RHS;

	public S2BinaryOp(S2Operator operator, S2Node LHS, S2Node RHS) {
		super(S2NodeKind.BINARY);
		this.operator = operator;
		this.LHS = LHS;
		this.RHS = RHS;
	}
}
