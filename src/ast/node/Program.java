package ast.node;

import java.util.ArrayList;
import java.util.List;

import ast.Node;
import ast.Visitor;

public class Program extends Node {
	public final List<Declaration> declarations = new ArrayList<>();
	
	public boolean runnable;
	
	@Override
	public void accept(Visitor v) {
		v.visit(this);
		for (Declaration d : declarations) {
			d.accept(v);
		}
		v.postVisit(this);
	}
}
