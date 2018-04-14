package semantic;

import ast.Logger;
import ast.TypeId;
import ast.Visitor;
import ast.node.Declaration;
import ast.node.Expr;
import ast.node.Program;
import ast.node.declare.FuncDeclaration;
import ast.node.declare.VarDeclaration;
import ast.node.expression.AssignExpr;
import ast.node.expression.BinaryExpr;
import ast.node.expression.CallExpr;
import ast.node.expression.IdExpr;
import ast.node.expression.IntegerExpr;
import ast.node.expression.SubscriptExpr;
import ast.node.statement.CompoundStmt;
import ast.node.statement.ExprStmt;
import ast.node.statement.IterationStmt;
import ast.node.statement.ReturnStmt;
import ast.node.statement.SelectStmt;

public class Checker extends Visitor {
	
	private Program program;
	private FuncDeclaration currentFunc;
	private Symtab symtab;
	
	public Checker(Symtab symtab) {
		this.symtab = symtab;
	}
	
	public void visit(Program p) {
		program = p;
	}
	
	public void postVisit(Program p) {
		Declaration d = symtab.get("main");
		if (d == null || d.isVar()) {
			Logger.INSTANCE.log("No main function declared");
		}
		if (p.getLast() != d) {
			Logger.INSTANCE.log("Main function not final declaration");
		}
		FuncDeclaration main = (FuncDeclaration) d;
		if (main.typeId != TypeId.VOID || !main.params.isEmpty()) {
			Logger.INSTANCE.log("Main function must be declared void main(void)");
		}
	}
	
	public void postVisit(SelectStmt s) {
		//If statements require boolean/integer condition
//		if (s.condition.getTypeId() != TypeId.INT || s.condition.isArray()) {
//			Logger.INSTANCE.log(s.condition, "If: Expected int expression not " 
//				+ s.condition.typeToString());
//		}
		
//		s.condition = s.condition.propagate();
//		s.affirmative = s.affirmative.propagate();
//		if (s.hasElse()) {
//			s.negative = s.negative.propagate();
//		}
	}
	
	public void postVisit(IterationStmt s) {
		//While statements require boolean/integer condition			
//		if (s.condition.getTypeId() != TypeId.INT || s.condition.isArray()) {
//			Logger.INSTANCE.log(s.condition, "While: Expected int expression not " 
//				+ s.condition.typeToString());
//		}
		
//		s.condition = s.condition.propagate();
//		s.body = s.body.propagate();
	}
	
	public void visit(FuncDeclaration d) {
		currentFunc = d;
		symtab.enterFunction();
		//No duplicate functions
		Declaration prev = symtab.putFunc(d);
		if (prev != null) {
			Logger.INSTANCE.log(d, String.format("Duplicate symbol %s "
				+ "(Previous declaration was at R: %d C: %d)",
				d.id, prev.start.row, prev.start.col));
		}
		
		//Add implicit return
		int stmtCount = d.body.statements.size();
		if (d.typeId == TypeId.VOID) {
			//There are no statements, OR, the last one does not return
			if (stmtCount == 0 || !d.body.getLast().returns()) {
				d.body.statements.add(new ReturnStmt(null, null, null));
			}
		}
	}
	
	public void postVisit(FuncDeclaration d) {
		//All code paths must return a value
//		if (d.typeId != TypeId.VOID && !d.body.returns()) {
//			Logger.INSTANCE.log(d, "Not all code paths return a value");
//		}
	}
	
	public void visit(CompoundStmt s) {
		symtab.enterBlock();
		
		//No statements after return
//		Iterator<Stmt> it = s.statements.iterator();
//		while (it.hasNext()) {
//			Stmt stmt = it.next();
//			if (stmt.returns() && it.hasNext()) {
//				Loc start = it.next().start;
//				Loc end = s.getLast().end;
//				Logger.INSTANCE.log(start, end, "Unreachable code");
//			}
//		}
	}
	
	public void postVisit(CompoundStmt s) {
		symtab.leaveBlock();
//		ListIterator<Stmt> iter = s.statements.listIterator();
//		while (iter.hasNext()) {
//			Stmt stmt = iter.next();
//			iter.remove();
//			iter.add(stmt.propagate());
//		}
	}
	
	public void postVisit(ReturnStmt s) {
		//Void functions cannot return values
		if (currentFunc.typeId == TypeId.VOID && s.hasReturnValue()) {
			Logger.INSTANCE.log(s.value, "Return: void functions cannot return a value");
		}
		
		//Non-void functions must return value
		if (currentFunc.typeId != TypeId.VOID && !s.hasReturnValue()) {
			Logger.INSTANCE.log(s, "Return: cannot return without value");
		}
		
		//Return expression type must match function type
		if (s.hasReturnValue() && s.value.getTypeId() != currentFunc.typeId) {
			Logger.INSTANCE.log(s.value, "Return: expected " + currentFunc.typeId + " expression not " + s.value.getTypeId());
		}
		
//		if (s.hasReturnValue()) {
//			s.value = s.value.propagate();	
//		}
	}
	
	public void visit(VarDeclaration d) {
		//Variables cannot be void
		if (d.typeId == TypeId.VOID) {
			Logger.INSTANCE.log(d, "Cannot declare variable as void");
		}
		//No duplicate variables
		//TODO Global shadowing?
		Declaration prev = symtab.putVar(d);
		if (prev != null) {
			Logger.INSTANCE.log(d, String.format("Duplicate symbol %s "
				+ "(Previous declaration was at R: %d C: %d)",
				d.id, prev.start.row, prev.start.col));
		}
	}
	
	public void postVisit(AssignExpr e) {
		//Left and Right hand sides must match type
		//Cannot assign to arrays
		if (e.left.isArray()) {
			Logger.INSTANCE.log(e.left, "Cannot assign to array");
		}
		//Types must match
		if (!e.right.typeEquals(e.left)) {
			Logger.INSTANCE.log(e.right, "Expected " + e.left.typeToString()
				+ " expression not " + e.right.typeToString());
		}
		
//		e.right = e.right.propagate();
	}
	
	public void postVisit(ExprStmt s) {
//		s.expr = s.expr.propagate();
	}
	
	public void postVisit(BinaryExpr e) {
		//No void or array children
		if (e.left.getTypeId() == TypeId.VOID || e.left.isArray()) {
			Logger.INSTANCE.log(e.left, "Expected int or float expression not " + e.right.typeToString());
		}
		if (e.right.getTypeId() == TypeId.VOID || e.right.isArray()) {
			Logger.INSTANCE.log(e.right, "Expected int or float expression not " + e.right.typeToString());
		}
		//No type coercion or mismatch
		if (e.right.getTypeId() != e.left.getTypeId()) {
			Logger.INSTANCE.log(e, "Incompatible expression types " + e.left.getTypeId() 
				+ " and " + e.right.getTypeId());
		}
		
//		e.right = e.right.propagate();
//		e.left = e.left.propagate();
	}
	
	public void postVisit(CallExpr e) {
		Declaration d = symtab.get(e.id);
		//Cannot call undeclared function
		if (d == null) {
			Logger.INSTANCE.log(e, "Undeclared function " + e.id);
		}
		
		//Cannot call variable
		if (d.isVar()) {
			Logger.INSTANCE.log(e, "Cannot call variable " + e.id);
		}
		
		FuncDeclaration f = (FuncDeclaration) d;
		e.func = f;
		
		//Actual parameter count must match formal parameter count
		int expectCount = f.params.size();
		int actualCount = e.arguments.size();
		if (expectCount != actualCount) {
			Logger.INSTANCE.log(e, "Expected " + expectCount + " arguments not " + actualCount);
		}
		
		//Actual parameter types must match formal parameter types
		int i = 0;
		while (i < actualCount && i < expectCount) {
			VarDeclaration expect = f.params.get(i);
			//Useful for the checking here
			IdExpr box = new IdExpr(null, null, expect.id);
			box.var = expect;
			Expr actual = e.arguments.get(i);
			
			if (!actual.typeEquals(box)) {
				Logger.INSTANCE.log(actual, "Expected " + box.typeToString() + " not " + actual.typeToString());
			}
			
//			e.params.set(i, actual.propagate());
			i++;
		}
	}
	
	public void postVisit(IdExpr e) {
		Declaration d = symtab.get(e.id);
		//Cannot use undeclared variables
		if (d == null) {
			Logger.INSTANCE.log(e, "Undeclared variable " + e.id);
		}
		
		//Functions are not identifiers proper
		if (!d.isVar()) {
			Logger.INSTANCE.log(e, "Function " + e.id + " cannot be used as identifier");
		}
		e.var = (VarDeclaration) d;
	}
	
	public void visit(IntegerExpr e) {
		//Ints cannot exceed 3 bytes
//		if (e.value >= SicXeGenUtil.WORD_MAX) {
//			Logger.INSTANCE.log(e, "Integer constant too large");
//		}
	}
	
	public void postVisit(SubscriptExpr e) {
		//Array index must be int
		if (e.index.getTypeId() != TypeId.INT || e.index.isArray()) {
			Logger.INSTANCE.log(e.index, "Index: Expected int expression not " + e.index.typeToString());
		}
				
		//Only arrays can have indices				
		if (!e.array.var.isArray()) {
			Logger.INSTANCE.log(e.index, "Index: cannot index non-array");
		}
		
//		e.index = e.index.propagate();
	}
}
