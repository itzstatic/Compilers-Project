package ast.node.declare;

import java.util.List;

import ast.Visitor;
import ast.node.Declaration;
import ast.node.statement.CompoundStmt;

public class FuncDeclaration extends Declaration {
	final public List<VarDeclaration> params;
	final public CompoundStmt body;
	
	public FuncDeclaration(List<VarDeclaration> params, CompoundStmt body) {
		this.params = params;
		this.body = body;
		
		int paramDisp = 0;
		for (VarDeclaration param : params) {
			param.disp = paramDisp;
			paramDisp += param.totalSize();
		}
	}

	public int getTotalParamSize() {
		int size = 0;
		for (VarDeclaration param : params) {
			size += param.totalSize();
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
}
