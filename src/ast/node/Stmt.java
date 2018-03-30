package ast.node;

import ast.Loc;
import ast.Node;

public abstract class Stmt extends Node {
	
	public Stmt(Loc start, Loc end) {
		super(start, end);
	}

	public abstract boolean returns();
	
	public Stmt propagate() {
		return this;
	}
}
