package semantic;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import ast.node.declare.FuncDeclaration;
import ast.node.declare.VarDeclaration;

public class Symtab {
	
	private Map<String, VarDeclaration> globals = new HashMap<>();
	private Map<String, FuncDeclaration> functions = new HashMap<>();
	private Map<String, VarDeclaration> params = new HashMap<>();
	private Stack<Map<String, VarDeclaration>> tables = new Stack<>();
	
	public VarDeclaration put(VarDeclaration d) {
		switch(d.scope) {
		case GLOBAL:
			return globals.put(d.id, d);
		case PARAM:
			return params.put(d.id, d);
		case LOCAL:
			VarDeclaration prev = tables.peek().get(d.id);
			if (prev != null) {
				return prev;
			}
			tables.peek().put(d.id, d);
			return null;
		}
		throw new IllegalStateException();
	}
	
	public VarDeclaration get(String id) {
		VarDeclaration result = globals.get(id);
		if (result != null) {
			return result;
		}
		result = params.get(id);
		if (result != null) {
			return result;
		}
		return getLocal(id);
	}
	
	public FuncDeclaration getFunction(String id) {
		return functions.get(id);
	}
	
	public FuncDeclaration putFunction(FuncDeclaration d) {
		return functions.put(d.id, d);
	}
	
	public VarDeclaration getLocal(String id) {
		for (Map<String, VarDeclaration> table : tables) {
			VarDeclaration result = table.get(id);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
	
	public void enterBlock() {
		tables.push(new HashMap<>());
	}
	
	public void leaveBlock() {
		tables.pop();
	}
	
	public void enterFunction(FuncDeclaration d) {
		functions.put(d.id, d);
		params.clear();
	}
}
