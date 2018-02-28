package ast.node.statement;

import ast.Visitor;
import ast.node.Expr;
import ast.node.Stmt;
import ast.node.expression.IntegerExpr;

public class IterationStmt extends Stmt {
	public Expr condition;
	public Stmt body;
	
	public IterationStmt(Expr condition, Stmt body) {
		this.condition = condition;
		this.body = body;
	}
	
	@Override
	public void accept(Visitor v) {
		v.visit(this);
		condition.accept(v);
		body.accept(v);
		v.postVisit(this);
	}

	@Override
	public boolean returns() {
		if (condition instanceof IntegerExpr) {
			return ((IntegerExpr) condition).value != 0;
		}
		return body.returns();
	}

	@Override
	public Stmt propagate() {
		if (condition instanceof IntegerExpr) {
			if (((IntegerExpr) condition).value == 0) {
				return body;
			}
		}
		return this;
	}
}
