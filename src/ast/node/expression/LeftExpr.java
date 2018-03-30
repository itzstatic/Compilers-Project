package ast.node.expression;

import ast.Loc;
import ast.node.Expr;

public abstract class LeftExpr extends Expr {

	public LeftExpr(Loc start, Loc end) {
		super(start, end);
	}

}
