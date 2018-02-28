package ast.node.statement;

import ast.node.Stmt;

public class NullStmt extends Stmt {

	@Override
	public boolean returns() {
		return false;
	}

}
