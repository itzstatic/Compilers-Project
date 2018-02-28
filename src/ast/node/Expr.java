package ast.node;

import ast.Node;
import ast.TypeId;

public abstract class Expr extends Node {
	//x = 2 + 3;
	//Returns the one propagated node, or this
	public Expr propagate() {
		return this;
	}
	
	public abstract TypeId getTypeId(); //What it evals to
	
	public boolean isArray() {
		return false;
	}
	
	public String typeToString() {
		return getTypeId().name()
			+ (this.isArray() ? "[]" : "");
	}
	
	public boolean typeEquals(Expr e) {
		return getTypeId() == e.getTypeId()
			&& this.isArray() == e.isArray();
	}
}
