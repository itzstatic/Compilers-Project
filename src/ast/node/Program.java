package ast.node;

import java.util.List;

import ast.Node;
import ast.Visitor;

public class Program extends Node {
	
	public final List<Declaration> declarations;
	
	public Program(List<Declaration> d) {
		super(d.get(0).start, d.get(d.size() - 1).end);
		declarations = d;
	}
	
	public Declaration getLast() {
		return declarations.get(declarations.size() - 1);
	}
	
	@Override
	public void accept(Visitor v) {
		v.visit(this);
		for (Declaration d : declarations) {
			d.accept(v);
		}
		v.postVisit(this);
	}
}
