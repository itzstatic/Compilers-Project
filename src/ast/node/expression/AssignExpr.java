package ast.node.expression;

import ast.TypeId;
import ast.Visitor;
import ast.node.Expr;

public class AssignExpr extends Expr {
	final public LeftExpr left;
	public Expr right;
	
	public AssignExpr(LeftExpr left, Expr right) {
		super(left.start, right.end);
		this.left = left;
		this.right = right;
	}
	
	@Override
	public TypeId getTypeId() {
		return right.getTypeId();
	}
	
	public void accept(Visitor v) {
		v.visit(this);
		left.accept(v);
		right.accept(v);
		v.postVisit(this);
	}
}
