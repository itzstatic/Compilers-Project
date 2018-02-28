package ast.node.statement;

import ast.Visitor;
import ast.node.Expr;
import ast.node.Stmt;

public class ReturnStmt extends Stmt {
	public Expr value;
	
	public ReturnStmt(Expr value) {
		this.value = value;
	}
	
	@Override
	public void accept(Visitor v) {
		v.visit(this);
		if (value != null) {
			value.accept(v);	
		}
		v.postVisit(this);
	}

	@Override
	public boolean returns() {
		return true;
	}
}
