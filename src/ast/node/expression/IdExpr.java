package ast.node.expression;

import ast.TypeId;
import ast.node.declare.VarDeclaration;

public class IdExpr extends LeftExpr {
	final public String id;
	
	public VarDeclaration var;
	
	public IdExpr(String id) {
		this.id = id;
	}
	
	@Override
	public TypeId getTypeId() {
		return var.typeId;
	}
	
	@Override
	public boolean isArray() {
		return var.isArray();
	}
	
	@Override
	public String toString() {
		return id;
	}
}
