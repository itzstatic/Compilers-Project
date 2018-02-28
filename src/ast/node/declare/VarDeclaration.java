package ast.node.declare;

import ast.Scope;
import ast.node.Declaration;

public class VarDeclaration extends Declaration {
	//If -1: Declared as var[], like in paramter
	//If 0: Declared as var, anywhere (Not an array)
	//If n: Declared as var[n], anywhere (Array)
	public final int dimension;
	public final Scope scope;
	
	public int disp;
	
	public VarDeclaration(Scope scope, boolean isArray) {
		this.scope = scope;
		dimension = isArray ? -1 : 0;
	} 
	
	public VarDeclaration(Scope scope, int dimension) {
		this.scope = scope;
		this.dimension = dimension;;
	}
	
	//This return value is based on the identifier
	public boolean isArray() {
		return dimension != 0;
	}
	
	public int totalSize() {
		if (dimension == 0) {
			return typeId.size;
		}
		if (dimension == -1) {
			return 3; //PTR
		}
		return typeId.size * dimension;
	}
	
	@Override
	public String toString() {
		if (dimension != 0) {
			return "D: " + typeId + " " + id + "[" + dimension + "]";	
		} else {
			return "D: " + typeId + " " + id;
		}
	}
}
