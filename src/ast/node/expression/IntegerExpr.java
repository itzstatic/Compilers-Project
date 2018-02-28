package ast.node.expression;

import ast.TypeId;
import ast.node.Expr;

public class IntegerExpr extends Expr {
	final public int value;

	public IntegerExpr(int value) {
		this.value = value;
	}
	
	@Override
	public TypeId getTypeId() {
		return TypeId.INT;
	}
}
