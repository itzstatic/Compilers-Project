package ast.node.expression;

import ast.Operation;
import ast.TypeId;
import ast.Visitor;
import ast.node.Expr;

public class BinaryExpr extends Expr {
	public Expr left;
	public Expr right;
	public final Operation op;
	
	public BinaryExpr(Expr left, Expr right, Operation op) {
		this.left = left;
		this.right = right;
		this.op = op;
	}

	@Override
	public Expr propagate() { 
		Expr result;
		if (left instanceof IntegerExpr && right instanceof IntegerExpr) {
			int l = ((IntegerExpr) left).value;
			int r = ((IntegerExpr) right).value;
			int v;
			switch(op){
			case ADD: v = l + r; break;
			case SUB: v = l - r; break;
			case MUL: v = l * r; break;
			case DIV: v = l / r; break;
			case EQ: v = (l == r ? 1 : 0); break;
			case GT: v = (l > r ? 1 : 0); break;
			case GTE: v = (l >= r ? 1 : 0); break;
			case LT: v = (l < r ? 1 : 0); break;
			case LTE: v = (l <= r ? 1 : 0); break;
			case NE: v = (l != r ? 1 : 0); break;
			default: throw new RuntimeException();
			}
			result = new IntegerExpr(v);
		} else if (left instanceof FloatExpr && right instanceof FloatExpr)  {
			double l = ((FloatExpr) left).value;
			double r = ((FloatExpr) right).value;
			double f = 0d;
			int i = 0;
			switch(op){
			case ADD: f = l + r; break;
			case SUB: f = l - r; break;
			case MUL: f = l * r; break;
			case DIV: f = l / r; break;
			case EQ: i = (l == r ? 1 : 0); break;
			case GT: i = (l > r ? 1 : 0); break;
			case GTE: i = (l >= r ? 1 : 0); break;
			case LT: i = (l < r ? 1 : 0); break;
			case LTE: i = (l <= r ? 1 : 0); break;
			case NE: i = (l != r ? 1 : 0); break;
			default: throw new RuntimeException();
			}
			if (op.isRelational()) {
				result = new IntegerExpr(i);
			} else {
				result = new FloatExpr(f);
			}					
		} else {
			return null;
		}
		result.start = left.start;
		result.end = right.end;
		return result;
	}
	
	@Override
	public TypeId getTypeId() {
		if (op.isRelational()) {
			return TypeId.INT; //Weakly typed C style
		} 
		return left.getTypeId(); //Arbitrarily use left, but right is identical
	}
	
	@Override
	public void accept(Visitor v) {
		v.visit(this);
		left.accept(v);
		right.accept(v);
		v.postVisit(this);
	}
}
