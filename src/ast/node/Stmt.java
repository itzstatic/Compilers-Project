package ast.node;

import ast.Node;

public abstract class Stmt extends Node {
	public abstract boolean returns();
	
	public Stmt propagate() {
		return this;
	}
}
