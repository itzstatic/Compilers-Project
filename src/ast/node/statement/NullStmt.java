package ast.node.statement;

import ast.Loc;
import ast.node.Stmt;

public class NullStmt extends Stmt {

	public NullStmt(Loc start) {
		super(start, new Loc(start.row, start.col + 1, start.pos + 1));
	}

	@Override
	public boolean returns() {
		return false;
	}

}
