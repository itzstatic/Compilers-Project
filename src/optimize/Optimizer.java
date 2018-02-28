package optimize;

import ast.Visitor;
import ast.node.expression.AssignExpr;
import ast.node.expression.BinaryExpr;
import ast.node.expression.SubscriptExpr;
import ast.node.statement.ExprStmt;
import ast.node.statement.IterationStmt;
import ast.node.statement.ReturnStmt;
import ast.node.statement.SelectStmt;

public class Optimizer extends Visitor {

	//Constant propagation
	public void postVisit(BinaryExpr e) {
		e.left = e.left.propagate();
		e.right = e.right.propagate();
	}
	
	public void postVisit(AssignExpr e) {
		e.right = e.right.propagate();
	}
	
	public void postVisit(SubscriptExpr e) {
		e.index = e.index.propagate();
	}
	
	public void postVisit(ExprStmt s) {
		s.expr = s.expr.propagate();
	}
	
	public void postVisit(IterationStmt s) {
		s.condition = s.condition.propagate();
	}
	
	public void postVisit(SelectStmt s) {
		s.condition = s.condition.propagate();
	}
	
	public void postVisit(ReturnStmt s) {
		s.value = s.value.propagate();
	}
}
