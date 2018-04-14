package parse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import ast.Loc;
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
		List<Declaration> declarations = new ArrayList<>();
		Declaration d;
		while ((d = declaration()) != null) {
			declarations.add(d);
		}
		if (lexer.peek() != null) {
			Logger.INSTANCE.log(location(), "Unexpected token: " + lexer.peek().payload);
		}
		if (declarations.isEmpty()) {
			Logger.INSTANCE.log("Expected declaration");
		}
		return new Program(declarations);
	}
	
	public Declaration declaration() {
		TypeId typeId = typeId();
		Loc start = lexer.start();
		if (typeId == null) {
			return null;
		}
		String id = lexer.expectId();
		if (lexer.accept(";")) {
			return new VarDeclaration(start, location(), typeId, 
				id, Scope.GLOBAL, false);
		} else if (lexer.accept("[")) {
			int dimension = lexer.expectInteger();
			lexer.expect("]");
			lexer.expect(";");
			return new VarDeclaration(start, location(), typeId, 
				id, Scope.GLOBAL, dimension);
		} else if (lexer.accept("(")){
			List<VarDeclaration> params = params();
			lexer.expect(")");
			CompoundStmt body = body();
			return new FuncDeclaration(start, location(), typeId, id, params, body);
		} else {
			Logger.INSTANCE.log(location(), "Expected declaration");
			return null;
		}
	}
	
	public CompoundStmt body() {
		lexer.expect("{");
		Loc start = lexer.start();
		List<VarDeclaration> locals = locals();
		List<Stmt> statements = statements();
		lexer.expect("}");
		return new CompoundStmt(start, location(), locals, statements);
	}

	public List<VarDeclaration> params() {
		TypeId typeId = requireTypeId();
		Loc start = lexer.location();
		String id;
		if (typeId == TypeId.VOID) {
			id = lexer.acceptId();
			if (id != null) {
				return paramList(start, typeId, id);
			} else {
				return new ArrayList<>();
			}
		} else {
			id = lexer.expectId();
			return paramList(start, typeId, id);
		}
	}
	
	public List<VarDeclaration> paramList(Loc start, TypeId typeId, String id) {
		List<VarDeclaration> result = new ArrayList<>();
		boolean isArray = false;
		if (lexer.accept("[")) {
			isArray = true;
			lexer.expect("]");
		}
		result.add(new VarDeclaration(start, location(), typeId, id, 
			Scope.PARAM, isArray));
		while (lexer.accept(",")) {
			typeId = requireTypeId();
			start = lexer.location();
			id = lexer.expectId();
			isArray = false;
			if (lexer.accept("[")) {
				isArray = true;
				lexer.expect("]");
			}
			result.add(new VarDeclaration(start, location(), typeId, id, 
				Scope.PARAM, isArray));
		}
		return result;
	}
	
	public List<VarDeclaration> locals() {
		List<VarDeclaration> result = new ArrayList<>();
		TypeId typeId;
		while ((typeId = typeId()) != null) {
			Loc start = lexer.start();
			String id = lexer.expectId();
			boolean isArray = false;
			int dimension = -1; //If array
			if (lexer.accept("[")) {
				isArray = true;
				dimension = lexer.expectInteger();
				lexer.accept("]");
			}
			lexer.expect(";");
			if (isArray) {
				result.add(new VarDeclaration(start, location(), typeId, id, 
					Scope.LOCAL, dimension));
			} else {
				result.add(new VarDeclaration(start, location(), typeId, id, 
					Scope.LOCAL, false));
			}
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
		Loc start;
		Expr expr;
		if (lexer.accept(";")) {
			start = lexer.start();
			return new NullStmt(start);
		} else if (lexer.accept("{")) {
			start = lexer.start();
			List<VarDeclaration> locals = locals();
			List<Stmt> statements = statements();
			lexer.expect("}");
			return new CompoundStmt(start, location(), locals, statements);
		} else if ((expr = expression()) != null) {
			lexer.expect(";");
			return new ExprStmt(location(), expr);
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
			return new SelectStmt(start, location(), condition, affirmative, negative);
		} else if (lexer.acceptKeyword("while")) {
			start = lexer.start();
			lexer.expect("(");
			Expr condition = require(this::expression);
			lexer.expect(")");
			Stmt body = requireStmt();
			return new IterationStmt(start, location(), condition, body);
		} else if (lexer.acceptKeyword("return")) {
			start = lexer.start();
			Expr value = expression();
			lexer.expect(";");
			return new ReturnStmt(start, location(), value);
		} else {
			return null;
		}
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
			} else {
				result = left;
			}
		} else if ((result = simple()) != null) {
		} else {
			return null;
		}
		return result;
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
					System.err.println("Parser#simple is doing it!");
				} else {
					result = null;
				}
				return new BinaryExpr(left, right, relop);
			} else {
				return left;
			}	
		} else {
			return null;
		}
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
			if ((right = termRest(factor())) != null) {
			} else if ((right = termRest(callOrVar())) != null) {
			} else {
				return null;
			}
			left = new BinaryExpr(left, right, addop);
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
	
	public Expr termRest(Expr left) {
		if (left == null) {
			return null;
		}
		Operation mulop;
		while ((mulop = mulOp()) != null) {
			Expr right;
			if ((right = factor()) != null) {
			} else if ((right = callOrVar()) != null) {
			} else {
				return null;
			}
			left = new BinaryExpr(left, right, mulop);
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
				return result;
			} else {
				return variable(id);
			}
		} else {
			return null;
		}
	}
	
	public Expr factor() {
		Integer integer;
		FloatNum floatnum;
		//Parenthesized expr
		if (lexer.accept("(")) {
			Expr result = require(this::expression);
			lexer.expect(")");
			return result;
		//Integer constant
		} else if ((integer = lexer.acceptInteger()) != null) {
			Loc start = lexer.start();
			return new IntegerExpr(start, location(), integer);
		//Float constant
		} else if ((floatnum = lexer.acceptFloat()) != null) {
			Loc start = lexer.start();
			return new FloatExpr(start, location(), floatnum.doubleValue());
		} else {
			return null;
		}
	}
	
	public Expr call(String id) {
		Loc start = lexer.start();
		if (lexer.accept("(")){
			List<Expr> args = new ArrayList<>();
			Expr arg = expression();
			if (arg != null) {
				while (lexer.accept(",")) {
					args.add(arg);
					arg = require(this::expression);
				}
				args.add(arg);
			}
			lexer.expect(")");
			Expr result = new CallExpr(start, location(), id, args);
			return result;
		} else {
			return null;
		}
	}
	
	public LeftExpr variable(String id) {
		IdExpr idExpr = new IdExpr(lexer.start(), location(), id);
		if (lexer.accept("[")) {
			Expr index = require(this::expression);
			lexer.expect("]");
			return new SubscriptExpr(location(), idExpr, index);
		} 
		return idExpr;
	}
	
	public Loc location() {
		return lexer.location();
	}
}
