package ast.node.statement;

import ast.Loc;
import ast.Visitor;
import ast.node.Expr;
import ast.node.Stmt;

public class ReturnStmt extends Stmt {
	public Expr value;
	
	public ReturnStmt(Loc start, Loc end, Expr value) {
		super(start, end);
		this.value = value;
	}
	
	public boolean hasReturnValue() {
		return value != null;
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
