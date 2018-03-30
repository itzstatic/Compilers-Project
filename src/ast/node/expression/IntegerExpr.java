package ast.node.expression;

import ast.Loc;
import ast.TypeId;
import ast.node.Expr;

public class IntegerExpr extends Expr {
	final public int value;

	public IntegerExpr(Loc start, Loc end, int value) {
		super(start, end);
		this.value = value;
	}
	
	@Override
	public TypeId getTypeId() {
		return TypeId.INT;
	}
}
