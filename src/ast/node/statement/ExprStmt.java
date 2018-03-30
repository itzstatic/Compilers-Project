package ast.node.statement;

import ast.Loc;
import ast.Visitor;
import ast.node.Expr;
import ast.node.Stmt;

public class ExprStmt extends Stmt {
	public Expr expr;

	public ExprStmt(Loc end, Expr expr) {
		super(expr.start, end);
		this.expr = expr;
	}
	
	@Override
	public void accept(Visitor v) {
		v.visit(this);
		expr.accept(v);
		v.postVisit(this);
	}

	@Override
	public boolean returns() {
		return false;
	}
	
}
