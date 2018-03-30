package ast.node;

import ast.Loc;
import ast.Node;
import ast.TypeId;
import ast.node.declare.VarDeclaration;

public abstract class Expr extends Node {
	
	public Expr(Loc start, Loc end) {
		super(start, end);
	}

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
		return getTypeId().name().toLowerCase()
			+ (this.isArray() ? "[]" : "");
	}
	
	public boolean typeEquals(Expr e) {
		return getTypeId() == e.getTypeId()
			&& this.isArray() == e.isArray();
	}
	
	public boolean typeEquals(VarDeclaration d) {
		return getTypeId() == d.typeId
			&& this.isArray() == d.isArray();
	}
}
