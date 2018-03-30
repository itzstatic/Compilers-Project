package ast.node;

import ast.Loc;
import ast.Node;
import ast.TypeId;

public abstract class Declaration extends Node {
	
	public final TypeId typeId;
	public final String id;
	
	public Declaration(Loc start, Loc end, TypeId typeId, String id) {
		super(start, end);
		this.typeId = typeId;
		this.id = id;
	}
	
	public abstract boolean isVar();
}
