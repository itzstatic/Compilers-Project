package ast.node.statement;

import ast.Loc;
import ast.Visitor;
import ast.node.Expr;
import ast.node.Stmt;
import ast.node.expression.IntegerExpr;

public class SelectStmt extends Stmt {
	public Expr condition;
	public Stmt affirmative;
	public Stmt negative;
	
	public SelectStmt(Loc start, Loc end, Expr condition, 
			Stmt affirmative, Stmt negative) {
		super(start, end);
		this.condition = condition;
		this.affirmative = affirmative;
		this.negative = negative;
	}
	
	public boolean hasElse() {
		return negative != null;
	}
	
	public boolean isTrue() {
		return condition instanceof IntegerExpr 
			&& ((IntegerExpr)condition).value != 0;
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
		if (hasElse()) {
			return affirmative.returns() && negative.returns();
		}
		if (condition instanceof IntegerExpr) {
			return ((IntegerExpr) condition).value != 0 && affirmative.returns();
		}
		return false;
	}

	@Override
	public Stmt propagate() {
		if (condition instanceof IntegerExpr) {
			boolean isTrue = ((IntegerExpr) condition).value != 0;
			if (isTrue) {
				return affirmative;
			}
			
			if (hasElse()) {
				return negative;
			} else {
				return new NullStmt(start);
			}
		}
		return this;
	}
	
}
