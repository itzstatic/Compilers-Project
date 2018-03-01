package parse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import ast.Location;
import ast.Logger;
import ast.Operation;
import ast.Scope;
import ast.TypeId;
import ast.node.Declaration;
import ast.node.Expr;
import ast.node.Program;
import ast.node.Stmt;
import ast.node.declare.FuncDeclaration;
import ast.node.declare.VarDeclaration;
import ast.node.expression.AssignExpr;
import ast.node.expression.BinaryExpr;
import ast.node.expression.CallExpr;
import ast.node.expression.FloatExpr;
import ast.node.expression.IdExpr;
import ast.node.expression.IntegerExpr;
import ast.node.expression.LeftExpr;
import ast.node.expression.SubscriptExpr;
import ast.node.statement.CompoundStmt;
import ast.node.statement.ExprStmt;
import ast.node.statement.IterationStmt;
import ast.node.statement.NullStmt;
import ast.node.statement.ReturnStmt;
import ast.node.statement.SelectStmt;


public class Parser {
	private Lexer lexer;
	
	public Parser(Lexer lexer) {
		this.lexer = lexer;
	}
	
	public Program program() {
		Program result = new Program();
		Declaration d;
		while ((d = declaration()) != null) {
			result.declarations.add(d);
		}
		if (lexer.peek() != null) {
			Logger.INSTANCE.log(location(), "Unexpected token: " + lexer.peek().payload);
		}
		return result;
	}
	
	public Declaration declaration() {
		Declaration result;
		TypeId typeId = typeId();
		Location start = lexer.start();
		if (typeId == null) {
			return null;
		}
		String id = lexer.expectId();
		if (lexer.accept(";")) {
			result = new VarDeclaration(Scope.GLOBAL, false);
		} else if (lexer.accept("[")) {
			result = new VarDeclaration(Scope.GLOBAL, lexer.expectInteger());
			lexer.expect("]");
			lexer.expect(";");
		} else if (lexer.accept("(")){
			List<VarDeclaration> params = params();
			lexer.expect(")");
			CompoundStmt body = body();
			result = new FuncDeclaration(params, body);
		} else {
			Logger.INSTANCE.log(location(), "Expected declaration");
			return null;
		}
		result.typeId = typeId;
		result.id = id;
		result.start = start;
		result.end = location();
		return result;
	}
	
	public CompoundStmt body() {
		CompoundStmt result;
		lexer.expect("{");
		Location start = lexer.start();
		List<VarDeclaration> locals = locals();
		List<Stmt> statements = statements();
		lexer.expect("}");
		result = new CompoundStmt(locals, statements);
		result.start = start;
		result.end = location();
		return result;
	}
	
	public List<VarDeclaration> params() {
		if (lexer.acceptKeyword("void")) {
			return new ArrayList<>();
		}
		List<VarDeclaration> result = new ArrayList<>();
		do {
			VarDeclaration param;
			TypeId typeId = requireTypeId();
			Location start = lexer.start();
			String id = lexer.expectId();
			if (lexer.accept("[")) {
				lexer.expect("]");
				param = new VarDeclaration(Scope.PARAM, true);
			} else {
				param = new VarDeclaration(Scope.PARAM, false);
			}
			param.typeId = typeId;
			param.id = id;
			param.start = start;
			param.end = lexer.location();
			result.add(param);
		} while (lexer.accept(","));
		return result;
	}
	
	public List<VarDeclaration> locals() {
		List<VarDeclaration> result = new ArrayList<>();
		TypeId typeId;
		String id;
		Location start;
		while ((typeId = typeId()) != null) {
			start = lexer.start();
			id = lexer.expectId();
			VarDeclaration local;
			if (lexer.accept("[")) {
				local = new VarDeclaration(Scope.LOCAL, lexer.expectInteger());
				lexer.accept("]");
			} else {
				local = new VarDeclaration(Scope.LOCAL, false);
			}
			lexer.expect(";");
			local.typeId = typeId;
			local.id = id;
			local.start = start;
			local.end = location();
			result.add(local);
		}
		return result;
	}
	
	public List<Stmt> statements() {
		List<Stmt> result = new ArrayList<>();
		Stmt s;
		while ((s = statement()) != null) {
			result.add(s);
		}
		return result;
	}
	
	public TypeId typeId() {
		if (lexer.acceptKeyword("int")) {
			return TypeId.INT;
		}
		if (lexer.acceptKeyword("float")) {
			return TypeId.FLOAT;
		}
		if (lexer.acceptKeyword("void")) {
			return TypeId.VOID;
		}
		return null;
	}
	
	public Stmt statement() {
		Stmt result;
		Location start;
		Expr expr;
		if (lexer.accept(";")) {
			start = lexer.start();
			result = new NullStmt();
		} else if (lexer.accept("{")) {
			start = lexer.start();
			List<VarDeclaration> locals = locals();
			List<Stmt> statements = statements();
			lexer.expect("}");
			result = new CompoundStmt(locals, statements);
		} else if ((expr = expression()) != null) {
			start = expr.start;
			lexer.expect(";");
			result = new ExprStmt(expr);
		} else if (lexer.acceptKeyword("if")) {
			start = lexer.start();
			lexer.expect("(");
			Expr condition = require(this::expression);
			lexer.expect(")");
			Stmt affirmative = requireStmt();
			Stmt negative = null;
			if (lexer.acceptKeyword("else")) {
				negative = requireStmt();
			}
			result = new SelectStmt(condition, affirmative, negative);
		} else if (lexer.acceptKeyword("while")) {
			start = lexer.start();
			lexer.expect("(");
			Expr condition = require(this::expression);
			lexer.expect(")");
			Stmt body = requireStmt();
			result = new IterationStmt(condition, body);
		} else if (lexer.acceptKeyword("return")) {
			start = lexer.start();
			Expr value = expression();
			lexer.expect(";");
			result = new ReturnStmt(value);
		} else {
			return null;
		}
		result.start = start;
		result.end = location();
		return result;
	}
	
	public TypeId requireTypeId() {
		TypeId result = typeId();
		if (result == null) {
			Logger.INSTANCE.log(location(), "Expected type specifier not " + lexer.peek().payload);
		}
		return result;
	}
	
	public Stmt requireStmt() {
		Stmt result = statement();
		if (result == null) {
			Logger.INSTANCE.log(location(), "Expected statement");
		}
		return result;
	}
	
	public <T> T require(Callable<T> nonterminal) {
		T result;
		try {
			result = nonterminal.call();
		} catch (Exception e) {
			Logger.INSTANCE.log(location(), e.getMessage());
			return null;
		}
		if (result == null) {
			Logger.INSTANCE.log(location(), "Expected expression");
		}
		return result;
	}
	
	public Expr expression() {
		Expr result;
		String id = lexer.acceptId();
		//(var|call) TermRest AdditiveRest
		if (id != null) {
			Expr left;
			Expr call;
			LeftExpr var;
			//Call
			if ((call = call(id)) != null){
				left = additiveRest(termRest(call));
			} else
			//Variable
			if ((var = variable(id)) != null) {
				//Assignment
				if (lexer.accept("=")) {
					Expr right = require(this::expression);
					left = new AssignExpr(var, right);
					left.start = var.start;
					left.end = right.end;
				//V' T' A'
				} else {
					left = additiveRest(termRest(var));
				}
			} else  {
				throw new IllegalStateException();
			}
			//Optional relop A
			//Left is definitely initialized
			Operation relOp = relOp();
			if (relOp != null) {
				Expr right = require(this::additive);
				result = new BinaryExpr(left, right, relOp);
				result.start = left.start;
				result.end = right.end;
			} else {
				result = left;
			}
		} else if ((result = simple()) != null) {
		} else {
			return null;
		}
		return result;
//		/////
//		if ((left = additiveRest(termRest(callOrVar()))) != null) {
//			Operation relop = relOp();
//			if (relop == null) {
//				result = left;
//			} else {
//				right = require(this::additive);
//				result = new BinaryExpr(left, right, relop);
//				result.start = left.start;
//				result.end = left.end;
//			}
//		}
//		if ((var = variable()) != null) {
//			Location start = lexer.start();
//			else if ((left = additiveRest(termRest(var))) != null) {
//				
//			//Basic var
//			} else {
//				//This will probably never happen
//				result = var;
//				throw new IllegalStateException();
//			}
//		//Simple expression
//		} 
//		return result;
	}
	
	public Expr simple() {
		Expr result;
		Expr left;
		Expr right;
		if ((left = additive()) != null) {
			Operation relop = relOp();
			if (relop != null) {
				if ((right = additive()) != null) {
				} else if ((right = additiveRest(termRest(callOrVar()))) != null) {
				} else {
					result = null;
				}
				result = new BinaryExpr(left, right, relop);
				result.start = left.start;
				result.end = right.end;
			} else {
				result = left;
			}	
		} else {
			result = null;
		}
		return result;
	}

	public Operation relOp() {
		if (lexer.accept("==")) {
			return Operation.EQ;
		}
		if (lexer.accept("!=")) {
			return Operation.NE;
		}
		if (lexer.accept(">=")) {
			return Operation.GTE;
		} 
		if (lexer.accept(">")) {
			return Operation.GT;
		} 
		if (lexer.accept("<")) {
			return Operation.LT;
		} 
		if (lexer.accept("<=")) {
			return Operation.LTE;	
		}
		return null;
	}
	
	public Expr additive() {
		Expr left = callOrVar();
		if (left == null) {
			left = factor();
		}
		return additiveRest(termRest(left));
	}
	
	public Expr additiveRest(Expr left) {
		if (left == null) {
			return null;
		}
		Operation addop;
		Expr right;
		while ((addop = addOp()) != null) {
			if ((right = term()) != null) {
			} else if ((right = termRest(callOrVar())) != null) {
			} else {
				return null;
			}
			Location start = left.start;
			left = new BinaryExpr(left, right, addop);
			left.start = start;
			left.end = right.end;
		}
		return left;
	}
	
	public Operation addOp() {
		if (lexer.accept("+")) {
			return Operation.ADD;
		} 
		if (lexer.accept("-")){
			return Operation.SUB;
		} 
		return null;
	}
	
	public Expr term() {
		Expr left = factor();
		return termRest(left);
	}
	
	public Expr termRest(Expr left) {
		if (left == null) {
			return null;
		}
		Operation mulop;
		Expr right;
		while ((mulop = mulOp()) != null) {
			Location start = left.start;
			if ((right = factor()) != null) {
			} else if ((right = callOrVar()) != null) {
			} else {
				return null;
			}
			left = new BinaryExpr(left, right, mulop);
			left.start = start;
			left.end = right.end;
		}
		return left;
	}
	
	public Operation mulOp() {
		if (lexer.accept("*")) {
			return Operation.MUL;
		}
		if (lexer.accept("/")) {
			return Operation.DIV;
		} 
		return null;
	}
	
	public Expr callOrVar() {
		Expr result;
		String id = lexer.acceptId();
		if (id != null) {
			if ((result = call(id)) != null) {
			} else {
				result = variable(id);
			}
		} else{
			return null;
		}
		return result;
	}
	
	public Expr factor() {
		Expr result;
		Integer integer;
		FloatNum floatnum;
		Location start;
		//Parenthesized expr
		if (lexer.accept("(")) {
			start = lexer.start();
			result = require(this::expression);
			lexer.expect(")");
		//Integer constant
		} else if ((integer = lexer.acceptInteger()) != null) {
			start = lexer.start();
			result = new IntegerExpr(integer);
		//Float constant
		} else if ((floatnum = lexer.acceptFloat()) != null) {
			start = lexer.start();
			result = new FloatExpr(floatnum.doubleValue());
		} else {
			return null;
		}
		result.start = start;
		result.end = location();
		return result;
	}
	
	public Expr call(String id) {
		Location start = lexer.start();
		if (lexer.accept("(")){
			List<Expr> params = new ArrayList<>();
			Expr param = expression();
			Location end = null;
			if (param != null) {
				end = param.end;
				while (lexer.accept(",")) {
					params.add(param);
					param = require(this::expression);
				}
				params.add(param);
			}
			lexer.expect(")");
			if (end == null) {
				end = lexer.location();
			}
			Expr result = new CallExpr(id, params);
			result.start = start;
			result.end = end;
			return result;
		} else {
			return null;
		}
	}
	
	public LeftExpr variable(String id) {
		LeftExpr result;
		Location start = lexer.start();
		if (lexer.accept("[")) {
			IdExpr array = new IdExpr(id);
			array.start = start;
			array.end = location();
			Expr index = require(this::expression);
			lexer.expect("]");
			result = new SubscriptExpr(array, index);
		} else {
			result = new IdExpr(id);
		}
		result.start = start;
		result.end = lexer.location();
		return result;
	}
	
	public Location location() {
		return lexer.location();
	}
}
