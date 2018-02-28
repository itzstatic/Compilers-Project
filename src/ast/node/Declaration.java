package ast.node;

import ast.Node;
import ast.TypeId;

public abstract class Declaration extends Node {
	public TypeId typeId;
	public String id;
}
