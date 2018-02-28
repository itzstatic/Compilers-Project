package ast.node.expression;

import ast.TypeId;
import ast.node.Expr;

public class FloatExpr extends Expr {

	final public double value;

	public FloatExpr(double value) {
		this.value = value;
	}
	
	@Override
	public TypeId getTypeId() {
		return TypeId.FLOAT;
	}

}
