package ast.node.expression;

import ast.Loc;
import ast.TypeId;
import ast.Visitor;
import ast.node.Expr;

public class SubscriptExpr extends LeftExpr {
	final public IdExpr array;
	public Expr index;
	
	public SubscriptExpr(Loc end, IdExpr array, Expr index) {
		super(array.start, end);
		this.array = array;
		this.index = index;
	}

	@Override
	public TypeId getTypeId() {
		return array.var.typeId;
	}
	
	@Override
	public String toString() {
		return array + "[]";
	}
	
	@Override
	public void accept(Visitor v) {
		v.visit(this);
		array.accept(v);
		index.accept(v);
		v.postVisit(this);
	}
}
