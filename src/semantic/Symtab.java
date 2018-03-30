package semantic;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import ast.node.Declaration;
import ast.node.declare.FuncDeclaration;
import ast.node.declare.VarDeclaration;

public class Symtab {
	
	private Map<String, Declaration> globals = new HashMap<>();
	//Only used in between seeing parameters, and seeing the open brace of the body
	private Map<String, VarDeclaration> params = new HashMap<>();
	//So apparently java.util.Stack is bugged foreach
	private Deque<Map<String, VarDeclaration>> tables = new ArrayDeque<>();
	
	public Declaration putVar(VarDeclaration d) {
		switch(d.scope) {
		case GLOBAL:
			return globals.put(d.id, d);
		case PARAM:
			return params.put(d.id, d);
		case LOCAL:
			if (tables.size() == 1) {
				VarDeclaration prev = params.get(d.id);
				if (prev != null) {
					return prev;
				}
			}
			return tables.peek().put(d.id, d);
		}
		throw new IllegalStateException();
	}
	
	public Declaration putFunc(FuncDeclaration d) {
		return globals.put(d.id, d);
	}
	
	public Declaration get(String id) {
		Declaration result;
		for (Map<String, VarDeclaration> table : tables) {
			result = table.get(id);
			if (result != null) {
				return result;
			}
		}
		result = params.get(id);
		if (result != null) {
			return result;
		}
		result = globals.get(id);
		if (result != null) {
			return result;
		}
		return null;
	}
	
	public void enterBlock() {
		tables.push(new HashMap<>());
	}
	
	public void leaveBlock() {
		tables.pop();
	}
	
	public void enterFunction() {
		params.clear();
	}
}
