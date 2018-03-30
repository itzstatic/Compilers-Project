package semantic;

import java.util.ArrayList;
import java.util.List;

import ast.Loc;
import ast.Scope;
import ast.TypeId;
import ast.node.declare.FuncDeclaration;
import ast.node.declare.VarDeclaration;

public class SymtabInit {
	
	private static final Loc NONE = new Loc(0, 0, -1);

	public void init(Symtab symtab) {
		FuncDeclaration input = new FuncDeclaration(NONE, NONE, TypeId.INT, 
			"input", new ArrayList<VarDeclaration>(), null);
		symtab.putFunc(input);
		
		List<VarDeclaration> params = new ArrayList<>();
		params.add(new VarDeclaration(NONE, NONE, TypeId.INT, "b", Scope.PARAM, false));
		FuncDeclaration output = new FuncDeclaration(NONE, NONE, TypeId.VOID,
			"output", params, null);
		symtab.putFunc(output);
	}
}
