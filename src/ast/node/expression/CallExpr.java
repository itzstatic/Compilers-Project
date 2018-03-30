package ast.node.expression;

import java.util.List;

import ast.Loc;
import ast.TypeId;
import ast.Visitor;
import ast.node.Expr;
import ast.node.declare.FuncDeclaration;

public class CallExpr extends Expr {

	public final String id; //Of the function
	public final List<Expr> params;
	
	//To be compiled
	public FuncDeclaration func;
	
	public CallExpr(Loc start, Loc end, String id, List<Expr> params) {
		super(start, end);
		this.id = id;
		this.params = params;
	}
	
	@Override
	public TypeId getTypeId() {
		return func.typeId;
	}
	
	@Override
	public void accept(Visitor v) {
		v.visit(this);
		for (Expr param : params) {
			param.accept(v);
		}
		v.postVisit(this);
	}
}
