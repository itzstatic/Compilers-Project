package gen.sicxe;

import gen.Block;
import gen.BlockWriter;
import gen.Frame;
import gen.block.SourceBlock;
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

public class SicXeGen {
	
	final private static int START = 0x100;
	
	//The number of locals on the current frame, after the params and retaddr
	private Frame frame = new Frame();
	protected Subroutines subs = new Subroutines();
	protected BlockWriter writer = new BlockWriter(subs);
	protected String name;
	
	public SicXeGen(String name) {
		this.name = name;
	}
	
	public Block gen(Program p) {
		writer.start();
		writer.write(String.format("%s start %x", name, START), 0);
		writer.comment("Basic important memory");
		writer.write("top word init", 3);
		writer.write("fr resf 1", 6);
		writer.write("ptr resw 1", 3);
		
		writer.start();
		for (Declaration d : p.declarations) {
			if (d instanceof FuncDeclaration) {
				writer.add(gen((FuncDeclaration)d));
			} else if (d instanceof VarDeclaration) {
				writer.add(gen((VarDeclaration)d));
			}
		}
		Block declarations = writer.end();
		
		if (subs.push) {
			writer.comment("Subroutine: push");
			writer.write("push sta @top", 3);
			writer.write("lda top", 3);
			writer.write("add #3", 3);
			writer.write("sta top", 3);
			writer.write("rsub", 3);
		}
		if (subs.pushf) {
			writer.comment("Subroutine: pushf");
			writer.write("pushf stf @top", 3);
			writer.write("lda top", 3);
			writer.write("add #6", 3);
			writer.write("sta top", 3);
			writer.write("rsub", 3);
		}
		if (subs.pop) {
			writer.comment("Subroutine: pop");
			writer.write("pop lda top", 3);
			writer.write("sub #3", 3);
			writer.write("sta top", 3);
			writer.write("lda @top", 3);
			writer.write("rsub", 3);
		}
		if (subs.popf) {
			writer.comment("Subroutine: popf");
			writer.write("popf lda top", 3);
			writer.write("sub #6", 3);
			writer.write("sta top", 3);
			writer.write("ldf @top", 3);
			writer.write("rsub", 3);
		}
		if (subs.set()) {
			writer.comment("Subroutines: set & unset");
			writer.write("set lda #1", 3);
			writer.write("rsub", 3);
			writer.write("unset lda #0", 3);
			writer.write("rsub", 3);
		}
		if (subs.setlt) {
			writer.comment("Subroutine: setlt");
			writer.write("setlt jlt set", 3);
			writer.write("j unset", 3);
		}
		if (subs.setgt) {
			writer.comment("Subroutine: setgt");
			writer.write("setgt jgt set", 3);
			writer.write("j unset", 3);
		}
		if (subs.setlte) {
			writer.comment("Subroutine: setlte");
			writer.write("setlte jgt unset", 3);
			writer.write("j set", 3);
		}
		if (subs.setgte) {
			writer.comment("Subroutine: setgte");
			writer.write("setgte jlt unset", 3);
			writer.write("j set", 3);
		}
		if (subs.seteq) {
			writer.comment("Subroutine: seteq");
			writer.write("seteq jeq set", 3);
			writer.write("j unset", 3);
		}
		if (subs.setne) {
			writer.comment("Subroutine: setne");
			writer.write("setne jeq unset", 3);
			writer.write("j set", 3);
		}
		writer.write("base frame", 0);
		writer.comment("BEGIN FUNCTION DECLARATIONS");
		writer.add(declarations);
		writer.write("nobase", 0);
		writer.write("ltorg", 0);
		writer.comment("Program Entry (start)");
		writer.comment("Initialize stack top and frame");
		writer.write("init ldb top", 3);
		writer.write("J _main", 4);
		writer.write("frame equ * + 2048", 0);
		writer.write("end init", 0);
		return writer.end();
	}
	
	public Block gen(FuncDeclaration d) {
		writer.start();
		writer.comment("Begin FuncDecl " + d.typeId + " " + d.id + "()");
		
		//Create param disps
		frame.incrementParam(d.getTotalParamSize());
		
		writer.comment("Push retaddr L onto stack");
		writer.write(String.format("_%s rmo l,a", d.id), 2);
		writer.pushWord();
		
		writer.add(gen(d.body));
		frame.decrementAllParams();
		writer.comment("End FuncDecl " + d.typeId + " " + d.id + "()");
		return writer.end();
	}
	
	public Block gen(VarDeclaration d) {
		writer.start();
		writer.comment("VarDecl " + d.typeId + " " + d.id);
		switch (d.scope) {
		case GLOBAL:
			int size = d.totalSize();
			switch(d.typeId) {
			case FLOAT: writer.write(String.format("_%s resf %d", d.id, size), size);
			case INT: 	writer.write(String.format("_%s resw %d", d.id, size), size);
			case VOID:
			}
			break;
		case LOCAL:
			//Nothing. Blocks increment by all its locals at once
		case PARAM:
			//Nothing. Caller increments params
		}
		return writer.end();
	}
	
	public Block gen(Stmt s) {
		if (s instanceof CompoundStmt) {
			return gen((CompoundStmt) s);
		}
		if (s instanceof ExprStmt) {
			return gen(((ExprStmt) s));
		} 
		if (s instanceof IterationStmt) {
			return gen((IterationStmt)s);
		}
		if (s instanceof ReturnStmt) {
			return gen((ReturnStmt)s);
		}
		if (s instanceof SelectStmt) {
			return gen((SelectStmt)s);
		}
		if (s instanceof NullStmt) {
			return new SourceBlock("", 0);
		}
		throw new IllegalStateException(s.getClass().getSimpleName());
	}
	
	public Block gen(ExprStmt s) {
		writer.start();
		writer.add(gen(s.expr));
		return writer.end();
	}
	
	public Block gen(CompoundStmt s) {
		writer.start();
		writer.comment("Enter Block Scope");
		
		//Gen local disps and grow localDisp
		int disp = frame.getFrameDisp();
		for (VarDeclaration local : s.locals) {
			local.disp = disp;
			disp += local.totalSize();
		}

		
		int size = s.getTotalLocalSize();
		writer.comment("Increment locals");	
		frame.incrementLocal(size);
		writer.increment(size);
		
		writer.comment("Body");
		for (Stmt t : s.statements) {
			writer.add(gen(t));
		}
		
		//Locals do not need to be decremented at run time, 
		//if the body is forced to return
		if (!s.returns()) {
			writer.comment("Decrement locals");
			writer.decrement(size);
		}
		frame.decrementLocal(size);
		
		writer.comment("Leave Block");
		return writer.end();
	}
	
	public Block gen(ReturnStmt s) {
		writer.start();
		writer.comment("Begin Return");
		if (s.hasReturnValue()) {
			writer.comment("Evaluate and Store Return Value");
			writer.add(gen(s.value));
			writer.moveAccToReg(s.value);
		}
		//Decrement locals
		writer.comment("Decrement all locals");
		writer.decrement(frame.getLocalDisp());
		//Frame should not decrement all locals because that is compile not runtime stuff
		
		//Return addr
		writer.comment("Store return address");
		writer.popWord();
		writer.write("rmo a,l", 2);
		
		writer.write("RSUB", 3);
		writer.comment("End Return");
		return writer.end();
	}
	
	public Block gen(IterationStmt s) {
		writer.start();
		Block cond = gen(s.condition);
		Block body = gen(s.body);
		writer.comment("Begin While Condition");
		writer.add(cond);
		writer.write("comp #0", 3);
		writer.write("jeq * + " + (body.size + 6), 3); //JEQ [END]
		writer.comment("Begin Do Body");
		writer.add(body);
		writer.write("j * - " + (body.size + cond.size + 9), 3); //J [WHILE]
		writer.comment("End While");
		return writer.end();
	}
	
	public Block gen(SelectStmt s) {
		writer.start();
		writer.comment("Begin If Condition");
		Block cond = gen(s.condition);
		Block aff = gen(s.affirmative);
		Block neg = s.hasElse() ? gen(s.negative) : null;
		writer.add(cond);
		writer.write("comp #0", 3);
		//If-End
		if (!s.hasElse()) {
			writer.write("jeq * + " + (aff.size + 3), 3); //JEQ [END]
			writer.comment("Begin If Body");
			writer.add(aff);
		//If-Else-End
		} else {
			boolean affRet = s.affirmative.returns();
			int jmp = aff.size + (affRet ? 3 : 6);
			writer.write("jeq * + " + jmp, 3); //JEQ [ELSE]
			writer.comment("Begin If Body");
			writer.add(aff);
			if (!affRet) {
				writer.write("j * + " + (neg.size + 3), 3); //J [END]
			}
			writer.comment("Begin Else Body");
			writer.add(neg);
		}
		writer.comment("End If");
		return writer.end();
	}
	
	public Block gen(Expr e) {
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
	
	public Block gen(AssignExpr e) {
		writer.start();
		writer.comment("Begin Assign " + e.left);
		
		LeftExpr left = e.left;
		if (left instanceof SubscriptExpr) {
			Expr index = ((SubscriptExpr) left).index;
			writer.comment("Evaluate Assignment Index");
			writer.add(gen(index));
			writer.push(index);
		}
		writer.comment("Evaluate Assignment RHS");
		writer.add(gen(e.right));

		StringBuilder store = new StringBuilder();
		SicXeGenUtil.buildStoreA(store, e.right);
		
		if (left instanceof SubscriptExpr) {
			VarDeclaration array = ((SubscriptExpr) left).array.var;
			SicXeGenUtil.buildPtr(store, array);
			Expr index = ((SubscriptExpr) left).index;
			writer.comment("Pop index and prepare to store");
			writer.moveAccToReg(e.right);
			writer.pop(index);
			writer.index(array);
			writer.moveRegToAcc(e.right);
		} else if (left instanceof IdExpr){
			SicXeGenUtil.buildVal(store, ((IdExpr) left).var);
		}
		
		writer.write(store.toString(), 3);
		return writer.end();
	}
	
	public Block gen(BinaryExpr e) {
		writer.start();
		writer.comment("Binary Expr Left Eval To stack");
		writer.add(gen(e.left));
		writer.push(e.left);
		
		writer.comment("Binary Expr Right Eval To reg");
		writer.add(gen(e.right));
		writer.moveAccToReg(e.right);
		writer.pop(e.left);
		
		writer.comment("Binary Expr: " + e.op);
		TypeId typeId = e.getTypeId();
		
		switch(e.op) {
		case ADD: 
			switch(typeId) {
			case FLOAT: writer.write("addf fr", 3); break;
			case INT: writer.write("addr s,a", 2); break;
			case VOID:
			}
			break;
		case SUB: 
			switch (typeId) {
			case FLOAT: writer.write("subf fr", 3); break;
			case INT: writer.write("subr s,a", 2); break;
			case VOID:
			}
			break;
		case MUL:
			switch (typeId) {
			case FLOAT: writer.write("mulf fr", 3); break;
			case INT: writer.write("mulr s,a", 2); break;
			case VOID:
			}
			break;
		case DIV:
			switch (typeId) {
			case FLOAT: writer.write("divf fr", 3); break;
			case INT: writer.write("divr s,a", 2); break;
			case VOID:
			}
			break;
		case EQ: writer.seteq(typeId); break;
		case NE: writer.setne(typeId); break;
		case LT: writer.setlt(typeId); break;
		case GT: writer.setgt(typeId); break;
		case LTE: writer.setlte(typeId); break;
		case GTE: writer.setgte(typeId); break;
		}
		if (e.op.isRelational() && e.left.getTypeId() == TypeId.FLOAT) {
			writer.write("float", 1); //Cast to float
		}
		writer.comment("End Binary Expr");
		return writer.end();
	}

	public Block gen(CallExpr e) {
		writer.start();
		writer.comment("Begin Call " + e.id + "()");
		for (Expr param : e.params) {
			writer.comment("Eval parameter");
			writer.add(gen(param));
			writer.push(param);
		}
		writer.comment("New frame");
		writer.write("lda top", 3);
		writer.write("sub #" + e.func.getTotalParamSize(), 3);
		writer.write("rmo a,b", 2);
		writer.comment("Jump");
		writer.write("+JSUB _" + e.func.id, 4);
		
		//Decrement by my frame size
		writer.comment("Restore frame");
		writer.write("sub #" + frame.getFrameDisp(), 3);
		writer.write("rmo a,b", 2);
		
		//Load return value
		if (e.func.typeId != TypeId.VOID) {
			writer.comment("Load Return value");
			writer.moveRegToAcc(e); //e.getTypeId() is func's typeId
		}
		
		writer.comment("End Call");
		return writer.end();
	}		
	
	public Block gen(IntegerExpr e) {
		writer.start();
		writer.comment("Integer literal " + e.value);
		int value = e.value;
		if (value < SicXeGenUtil.IMMEDIATE_MAX) {
			writer.write("lda #" + e.value, 3);	
		} else if (value < SicXeGenUtil.ABSOLUTE_MAX) {
			writer.write("+lda #" + e.value, 4);
		} else if (value < SicXeGenUtil.WORD_MAX) {
			writer.write(String.format("lda =X'%06X'", e.value), 3);
		} 
		return writer.end();
	}
	
	public Block gen(FloatExpr e) {
		writer.start();
		writer.comment("Float literal " + e.value);
		long bits = SicXeGenUtil.doubleToSicXeBits(e.value);
		writer.write(String.format("ldf =X'%012X'", bits), 3);
		return writer.end();
	}
	
	public Block gen(IdExpr e) {
		writer.start();
		writer.comment("Identifier " + e.id);
		VarDeclaration var = e.var;
		if (var.isArray()) {
			//Load address of array
			switch (var.scope) {
			case GLOBAL: writer.write(String.format("lda #_%s", var.id), 3); break;
			case LOCAL:
				writer.write(String.format("lda #frame + %d", var.disp), 3);
				//writer.write("addr b,a", 2);
				break;
			case PARAM: writer.write(String.format("lda frame + %d", var.disp), 3); break;
			}
		} else { //Load value of variable
			StringBuilder load = new StringBuilder();
//			if (var.scope == Scope.GLOBAL) {
//				loadInstr.append('+');
//			}
			SicXeGenUtil.buildLoadA(load, e);
			SicXeGenUtil.buildVal(load, var);
			//write(loadInstr.toString(), var.scope == Scope.GLOBAL ? 4 : 3);
			writer.write(load.toString(),3);
		}
		return writer.end();
	}
	
	public Block gen(SubscriptExpr e) {
		writer.start();
		VarDeclaration var = e.array.var;
		writer.comment("Subscript");
		writer.add(gen(e.index));
		writer.index(var);
		
		StringBuilder load = new StringBuilder();
		SicXeGenUtil.buildLoadA(load, e);
		SicXeGenUtil.buildPtr(load, var);
		writer.write(load.toString(), 3);
		
		return writer.end();
	}
}
