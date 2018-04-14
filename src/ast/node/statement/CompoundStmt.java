package ast.node.statement;

import java.util.List;

import ast.Loc;
import ast.Sizes;
import ast.Visitor;
import ast.node.Stmt;
import ast.node.declare.VarDeclaration;

public class CompoundStmt extends Stmt {
	final public List<VarDeclaration> locals;
	final public List<Stmt> statements;
	
	public CompoundStmt(Loc start, Loc end, List<VarDeclaration> locals, 
			List<Stmt> statements) {
		super(start, end);
		this.locals = locals;
		this.statements = statements;
	}
	
	public int getTotalLocalSize(Sizes sizes) {
		int size = 0;
		for (VarDeclaration local : locals) {
			size += local.totalSize(sizes);
		}
		return size;
	}
	
	//Or null
	public Stmt getLast() {
		int size = statements.size();
		if (size > 0) {
			return statements.get(size - 1);
		}
		return null;
	}
	
	@Override
	public void accept(Visitor v) {
		v.visit(this);
		for (VarDeclaration local : locals) {
			local.accept(v);
		}
		for (Stmt s : statements) {
			s.accept(v);
		}
		v.postVisit(this);
	}

	@Override
	public boolean returns() {
		for (Stmt s : statements) {
			if (s.returns()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Stmt propagate() {
		if (statements.size() == 0) {
			return new NullStmt(start);
		} 
		if (statements.size() == 1) {
			return statements.get(0);
		}
		return this;
	}
}
