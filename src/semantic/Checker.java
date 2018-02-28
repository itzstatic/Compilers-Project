package semantic;

import gen.SicXeGenUtil;

import java.util.Iterator;

import ast.Location;
import ast.Logger;
import ast.TypeId;
import ast.Visitor;
import ast.node.Expr;
import ast.node.Program;
import ast.node.Stmt;
import ast.node.declare.FuncDeclaration;
import ast.node.declare.VarDeclaration;
import ast.node.expression.AssignExpr;
import ast.node.expression.BinaryExpr;
import ast.node.expression.CallExpr;
import ast.node.expression.IdExpr;
import ast.node.expression.IntegerExpr;
import ast.node.expression.SubscriptExpr;
import ast.node.statement.CompoundStmt;
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
		//Programs must have at least one declaration
		if (p.declarations.size() < 1) {
			Logger.INSTANCE.log("Expected declaration");
		}
	}
	
	public void postVisit(Program p) {
		if (!p.runnable) {
			Logger.INSTANCE.log("No main function declared");
		}
	}
	
	public void visit(SelectStmt s) {
		//If statements require boolean/integer condition
		if (s.condition.getTypeId() != TypeId.INT || s.condition.isArray()) {
			Logger.INSTANCE.log(s.condition, "If: Expected int expression not " 
				+ s.condition.typeToString());
		}
	}
	
	public void visit(IterationStmt s) {
		//While statements require boolean/integer condition
		if (s.condition.getTypeId() != TypeId.INT || s.condition.isArray()) {
			Logger.INSTANCE.log(s.condition, "While: Expected int expression not " 
				+ s.condition.typeToString());
		}
	}
	
	public void visit(FuncDeclaration d) {
		currentFunc = d;
		symtab.enterFunction(d);
		
		//All code paths must return a value
		if (d.typeId != TypeId.VOID && !d.body.returns()) {
			Logger.INSTANCE.log(d, "Not all code paths return a value");
		}
		
		//Cache main
		if (d.id.equals("main")) {
			program.runnable = true;
		}
		
		//Implicit return
		int stmtCount = d.body.statements.size();
		//There are statements and it returns void and the last statements is not return
		//OR there are no statements
		if ((stmtCount > 0 && d.typeId == TypeId.VOID 
				&& !(d.body.getLast() instanceof ReturnStmt)) 
			|| (stmtCount == 0)) {
			d.body.statements.add(new ReturnStmt(null));
		}
	}
	
	public void visit(CompoundStmt s) {
		symtab.enterBlock();
		
		//No statements after return
		Iterator<Stmt> it = s.statements.iterator();
		while (it.hasNext()) {
			Stmt stmt = it.next();
			if (stmt.returns() && it.hasNext()) {
				Location start = it.next().start;
				Location end = s.getLast().end;
				Logger.INSTANCE.log(start, end, "Dead code");
			}
		}
	}
	
	public void postVisit(CompoundStmt s) {
		symtab.leaveBlock();
	}
	
	public void postVisit(ReturnStmt s) {
		//Void functions cannot return values
		if (currentFunc.typeId == TypeId.VOID && s.value != null) {
			Logger.INSTANCE.log(s.value, "Return: void functions cannot return a value");
		}
		
		//Non-void functions must return value
		if (currentFunc.typeId != TypeId.VOID && s.value == null) {
			Logger.INSTANCE.log(s, "Return: cannot return without value");
		}
		
		//Return expression type must match function type
		if (s.value != null && s.value.getTypeId() != currentFunc.typeId) {
			Logger.INSTANCE.log(s.value, "Return: expected " + currentFunc.typeId + " expression not " + s.value.getTypeId());
		}
	}
	
	public void visit(VarDeclaration d) {
		//Variables cannot be void
		if (d.typeId == TypeId.VOID) {
			Logger.INSTANCE.log(d, "Cannot declare variable as void");
		}
		//No duplicate variables
		//TODO Global shadowing?
		VarDeclaration prev = symtab.put(d);
		if (prev != null) {
			Logger.INSTANCE.log(d, String.format("Duplicate variable %s "
				+ "(Previous declaration was at R: %d C: %d)",
				d.id, prev.start.row, prev.start.col));
		}
	}
	
	public void postVisit(AssignExpr e) {
		//Left and Right hand sides must match type
		//TODO reassign arrays?
		//Cannot assign to arrays
		if (e.left.isArray()) {
			Logger.INSTANCE.log(e.left, "Cannot assign to array");
		}
		//Types must match
		if (!e.right.typeEquals(e.left)) {
			Logger.INSTANCE.log(e.right, "Expected " + e.left.typeToString()
				+ " expression not " + e.right.typeToString());
		}
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
	}
	
	public void postVisit(CallExpr e) {
		//Cannot call undeclared function
		FuncDeclaration f = symtab.getFunction(e.id);
		if (f == null) {
			Logger.INSTANCE.log(e, "Undeclared function " + e.id);
		}
		e.func = f;
		
		//Actual parameter count must match formal parameter count
		int expectCount = f.params.size();
		int actualCount = e.params.size();
		if (expectCount != actualCount) {
			Logger.INSTANCE.log(e, "Expected " + expectCount + " arguments not " + actualCount);
		}
		
		//Actual parameter types must match formal parameter types
		int i = 0;
		while (i < actualCount && i < expectCount) {
			VarDeclaration expect = f.params.get(i);
			//Useful for the checking here
			IdExpr box = new IdExpr(expect.id);
			box.var = expect;
			Expr actual = e.params.get(i);
			
			if (!actual.typeEquals(box)) {
				Logger.INSTANCE.log(actual, "Expected " + box.typeToString() + " not " + actual.typeToString());
			}
			
			i++;
		}
	}
	
	public void postVisit(IdExpr e) {
		//Cannot use undeclared variables
		VarDeclaration var  = symtab.get(e.id);
		if (var == null) {
			Logger.INSTANCE.log(e, "Undeclared variable " + e.id);
		}
		e.var = var;
	}
	
	public void visit(IntegerExpr e) {
		//Ints cannot exceed 3 bytes
		if (e.value >= SicXeGenUtil.WORD_MAX) {
			Logger.INSTANCE.log(e, "Integer constant too large");
		}
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
	}
}
