package ast.node.declare;

import java.util.List;

import ast.Loc;
import ast.Sizes;
import ast.TypeId;
import ast.Visitor;
import ast.node.Declaration;
import ast.node.statement.CompoundStmt;

public class FuncDeclaration extends Declaration {
	final public List<VarDeclaration> params;
	final public CompoundStmt body;
	
	public FuncDeclaration(Loc start, Loc end, TypeId typeId, String id,
			List<VarDeclaration> params, CompoundStmt body) {
		super(start, end, typeId, id);
		this.params = params;
		this.body = body;
	}

	public int getTotalParamSize(Sizes sizes) {
		int size = 0;
		for (VarDeclaration param : params) {
			size += param.totalSize(sizes);
		}
		return size;
	}
	
	public void accept(Visitor v) {
		v.visit(this);
		for (VarDeclaration param : params) {
			param.accept(v);
		}
		body.accept(v);
		v.postVisit(this);
	}

	@Override
	public boolean isVar() {
		return false;
	}
}
