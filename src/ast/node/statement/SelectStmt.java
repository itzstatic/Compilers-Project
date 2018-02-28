package ast.node.statement;

import ast.Visitor;
import ast.node.Expr;
import ast.node.Stmt;
import ast.node.expression.IntegerExpr;

public class SelectStmt extends Stmt {
	public Expr condition;
	public Stmt affirmative;
	public Stmt negative;
	
	public SelectStmt(Expr condition, Stmt affirmative, Stmt negative) {
		this.condition = condition;
		this.affirmative = affirmative;
		this.negative = negative;
	}
	
	@Override
	public void accept(Visitor v) {
		v.visit(this);
		condition.accept(v);
		affirmative.accept(v);
		if (negative != null) {
			negative.accept(v);
		}
		v.postVisit(this);
	}

	@Override
	public boolean returns() {
		if (negative == null) {
			return false; //TODO Return whether condition is true (static eval)
		}
		return affirmative.returns() && negative.returns();
	}

	@Override
	public Stmt propagate() {
		if (condition instanceof IntegerExpr) {
			boolean isTrue = ((IntegerExpr) condition).value != 0;
			//If
			if (negative == null) {
				if (isTrue) {
					return affirmative;
				} else {
					return new NullStmt();
				}
			//If-Else
			} else {
				if (isTrue) {
					return affirmative;
				} else {
					return negative;
				}
			}
		}
		return this;
	}
	
}
