package ast.node.declare;

import ast.Loc;
import ast.Scope;
import ast.Sizes;
import ast.TypeId;
import ast.node.Declaration;

public class VarDeclaration extends Declaration {
	//If -1: Declared as var[], like in paramter
	//If 0: Declared as var, anywhere (Not an array)
	//If n: Declared as var[n], anywhere (Array)
	public final int dimension;
	public final Scope scope;
	
	public int disp;
	
	public VarDeclaration(Loc start, Loc end, TypeId typeId, String id, 
			Scope scope, boolean isArray) {
		super(start, end, typeId, id);
		this.scope = scope;
		dimension = isArray ? -1 : 0;
	} 
	
	public VarDeclaration(Loc start, Loc end, TypeId typeId, String id,
			Scope scope, int dimension) {
		super(start, end, typeId, id);
		this.scope = scope;
		this.dimension = dimension;;
	}
	
	//This return value is based on the identifier
	public boolean isArray() {
		return dimension != 0;
	}
	
	public int totalSize(Sizes sizes) {
		if (dimension == 0) {
			return sizes.sizeOf(typeId);
		}
		if (dimension == -1) {
			return sizes.sizeOfPtr();
		}
		return sizes.sizeOf(typeId) * dimension;
	}
	
	@Override
	public String toString() {
		if (dimension != 0) {
			return "D: " + typeId + " " + id + "[" + dimension + "]";	
		} else {
			return "D: " + typeId + " " + id;
		}
	}

	@Override
	public boolean isVar() {
		return true;
	}
}
