package ast.node.expression;

import ast.Loc;
import ast.TypeId;
import ast.node.Expr;

public class FloatExpr extends Expr {

	final public double value;

	public FloatExpr(Loc start, Loc end, double value) {
		super(start, end);
		this.value = value;
	}
	
	@Override
	public TypeId getTypeId() {
		return TypeId.FLOAT;
	}

}
