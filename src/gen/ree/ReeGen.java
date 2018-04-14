package gen.ree;

import java.util.ArrayList;
import java.util.List;

import ast.Operation;
import ast.Sizes;
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
import ast.node.expression.SubscriptExpr;
import ast.node.statement.CompoundStmt;
import ast.node.statement.ExprStmt;
import ast.node.statement.IterationStmt;
import ast.node.statement.NullStmt;
import ast.node.statement.ReturnStmt;
import ast.node.statement.SelectStmt;

public class ReeGen {
	
	public final List<Quad> quads = new ArrayList<>();
	
	private Sizes sizes = new ReeSizes();
	private int counter;
	
	public void gen(Program p) {
		for (Declaration d : p.declarations) {
			if (d instanceof VarDeclaration) {
				gen((VarDeclaration)d);
			} else if (d instanceof FuncDeclaration) {
				gen((FuncDeclaration)d);
			}
		}
	}
	
	private void gen(VarDeclaration d) {
		String byteCount = Integer.toString(d.totalSize(sizes));
		Quad q;
		switch (d.scope) {
		case GLOBAL:
		case LOCAL: q = new Quad(Mnemonic.ALLOC, byteCount, null, d.id); break;
		case PARAM: q = new Quad(Mnemonic.PARM, byteCount, null, d.id); break;
		default: throw new IllegalStateException();
		}
		quads.add(q);
	}
	
	private void gen(FuncDeclaration d) {
		quads.add(new Quad(Mnemonic.FUNC, 
			d.typeId.toString(), 
			Integer.toString(d.params.size()), 
			d.id));
		for (VarDeclaration param : d.params) {
			gen(param);
		}
		gen(d.body);
		quads.add(new Quad(Mnemonic.END, "func"));
	}
	
	private void gen(Stmt s) {
		if (s instanceof CompoundStmt) {
			gen((CompoundStmt) s);
		} else if (s instanceof ExprStmt) {
			gen(((ExprStmt) s));
		} else if (s instanceof IterationStmt) {
			gen((IterationStmt)s);
		} else if (s instanceof ReturnStmt) {
			gen((ReturnStmt)s);
		} else if (s instanceof SelectStmt) {
			gen((SelectStmt)s);
		} else if (!(s instanceof NullStmt)) {
			throw new IllegalStateException(s.getClass().getSimpleName());
		}
	}
	
	private void gen(CompoundStmt s) {
		quads.add(new Quad(Mnemonic.BLOCK));
		for (VarDeclaration local : s.locals) {
			gen(local);
		}
		for (Stmt stmt : s.statements) {
			gen(stmt);
		}
		quads.add(new Quad(Mnemonic.END, "block"));
	}
	
	//Assumes ONLY a relop is the root of the condition
	private void gen(IterationStmt s) {
		BinaryExpr binary = (BinaryExpr)s.condition;
		
		//Jump when FALSE
		Mnemonic mnemonic = relOpToOppositeMnemonic(binary.op);
		
		int begin = quads.size();
		String left = gen(binary.left);
		String right = gen(binary.right);
		String compare = "t" + counter++;
		
		quads.add(new Quad(Mnemonic.COMP, left, right, compare));
		Quad skipBody = new Quad(mnemonic, compare);
		quads.add(skipBody);
		
		gen(s.body);
		
		quads.add(new Quad(Mnemonic.B, null, null, Integer.toString(begin)));
		
		//Backpatch end
		skipBody.result = Integer.toString(quads.size());
	}
	
	private void gen(ExprStmt s) {
		gen(s.expr);
	}
	
	private void gen(ReturnStmt s) {
		String value = s.hasReturnValue() ? gen(s.value) : null;
		quads.add(new Quad(Mnemonic.RET, null, null, value));
	}
	
	//Same assumption as for loops
	private void gen(SelectStmt s) {
		BinaryExpr binary = (BinaryExpr)s.condition;
		Mnemonic mnemonic = relOpToOppositeMnemonic(binary.op);
		
		String left = gen(binary.left);
		String right = gen(binary.right);
		String compare = "t" + counter++;
		
		quads.add(new Quad(Mnemonic.COMP, left, right, compare));
		Quad skipTrue = new Quad(mnemonic, compare);
		quads.add(skipTrue);
		
		gen(s.affirmative);
		
		Quad skipFalse = null;
		if (s.hasElse()) {
			skipFalse = new Quad(Mnemonic.B);
			quads.add(skipFalse);
		}
		
		skipTrue.result = Integer.toString(quads.size()); //Backpatch
		
		if (s.hasElse()) {
			gen(s.negative);
			skipFalse.result = Integer.toString(quads.size());
		}
	}

	private String gen(Expr e) {
		if (e instanceof AssignExpr) {
			return gen(((AssignExpr) e));
		}
		if (e instanceof BinaryExpr) {
			return gen(((BinaryExpr) e));
		}
		if (e instanceof CallExpr) {
			return gen(((CallExpr) e));
		}
		if (e instanceof IntegerExpr) {
			return gen(((IntegerExpr) e));
		}
		if (e instanceof FloatExpr) {
			return gen((FloatExpr)e);
		}
		if (e instanceof IdExpr) {
			return gen(((IdExpr) e));
		}
		if (e instanceof SubscriptExpr) {
			return gen((SubscriptExpr) e);
		}
		throw new IllegalStateException(e.getClass().getSimpleName());
	}
	
	private String gen(AssignExpr e) {
		//This just so-happens to work for subscript and id left's
		String left = gen(e.left);
		String right = gen(e.right);
		quads.add(new Quad(Mnemonic.ASSIGN, right, null, left));
		return left;
		
		//		if (e.left instanceof IdExpr) {
//			return gen(e, (IdExpr)e.left);
//		} else if (e.left instanceof SubscriptExpr) {
//			return gen(e, (SubscriptExpr)e.left);
//		}
//		throw new IllegalStateException();
	}
	
//	private String gen(AssignExpr e, IdExpr left) {
//		String right = gen(e.right);
//		quads.add(new Quad(Mnemonic.ASSIGN, right, null, left.id));
//		return left.id;
//	}
//	
//	private String gen(AssignExpr e, SubscriptExpr left) {
//		String addr = gen(e.left);
//		String right = gen(e.right);
//		quads.add(new Quad(Mnemonic.ASSIGN, right, null, addr));
//		return addr;
//	}
	
	private String gen(BinaryExpr e) {
		String left = gen(e.left);
		String right = gen(e.right);
		Mnemonic mnemonic;
		switch(e.op) {
		case ADD: mnemonic = Mnemonic.ADD; break;
		case SUB: mnemonic = Mnemonic.SUB; break;
		case MUL: mnemonic = Mnemonic.MUL; break;
		case DIV: mnemonic = Mnemonic.DIV; break;
		default: throw new IllegalStateException();
		}
		String result = "t" + counter++;
		quads.add(new Quad(mnemonic, left, right, result));
		return result;
	}
	
	private String gen(CallExpr e) {
		for (Expr arg : e.arguments) {
			quads.add(new Quad(Mnemonic.ARG, gen(arg)));
		}
		String result = "t" + counter++;
		quads.add(new Quad(
			Mnemonic.CALL, 
			e.id, 
			Integer.toString(e.arguments.size()), 
			result));
		return result;
	}
	
	private String gen(IntegerExpr e) {
		return Integer.toString(e.value);
	}
	
	private String gen(FloatExpr e) {
		return Double.toString(e.value);
	}
	
	private String gen(IdExpr e) {
		return e.id;
	}
	
	private String gen(SubscriptExpr e) {
		//TODO runtime multiplication when index is not int constant?
		String index = gen(e.index);
		String disp = "t" + counter++;
		String result = "t" + counter++;
		quads.add(new Quad(
			Mnemonic.MUL, 
			index, 
			Integer.toString(sizes.sizeOf(e.array.getTypeId())),
			disp));
		quads.add(new Quad(Mnemonic.DISP, e.array.id, disp, result));	
		return result;
	}
	
	private Mnemonic relOpToOppositeMnemonic(Operation relOp) {
		switch(relOp) {
		case EQ: return Mnemonic.BNE; 
		case GT: return Mnemonic.BLE; 
		case GTE:return Mnemonic.BLT; 
		case LT: return Mnemonic.BGE; 
		case LTE:return Mnemonic.BGT; 
		case NE: return Mnemonic.BEQ; 
		default: throw new IllegalStateException();
		}
	}
}
