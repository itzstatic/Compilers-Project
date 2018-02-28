package gen;

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
import ast.node.statement.ReturnStmt;
import ast.node.statement.SelectStmt;

public class SicXeGen {
	
	final private static int START = 0x100;
	
	//The number of locals on the current frame, after the params and retaddr
	private Frame frame = new Frame();
	private Subroutines subs = new Subroutines();
	private BlockWriter writer = new BlockWriter(subs);
	
	public Block gen(Program p) {
		writer.start();
		writer.write("cminus start " + Integer.toString(START, 16), 0);
		writer.comment("Basic important memory");
		writer.write("_top word _start", 3);
		writer.write("_fr resf 1", 6);
		writer.write("_ptr resw 1", 3);
		
		writer.start();
		for (Declaration d : p.declarations) {
			//d.disp = START + writer.size();
			if (d instanceof FuncDeclaration) {
				writer.add(gen((FuncDeclaration)d));
			}
		}
		Block functions = writer.end();
		
		writer.start();
		for (Declaration d : p.declarations) {
			if (d instanceof VarDeclaration) {
				writer.add(gen((VarDeclaration)d));
			}
		}
		Block globals = writer.end();
		
		if (subs.push) {
			writer.comment("Subroutine: push");
			writer.write("_push sta @_top", 3);
			writer.write("lda _top", 3);
			writer.write("add #3", 3);
			writer.write("sta _top", 3);
			writer.write("rsub", 3);
		}
		if (subs.pushf) {
			writer.comment("Subroutine: pushf");
			writer.write("_pushf stf @_top", 3);
			writer.write("lda _top", 3);
			writer.write("add #6", 3);
			writer.write("sta _top", 3);
			writer.write("rsub", 3);
		}
		if (subs.pop) {
			writer.comment("Subroutine: pop");
			writer.write("_pop lda _top", 3);
			writer.write("sub #3", 3);
			writer.write("sta _top", 3);
			writer.write("lda @_top", 3);
			writer.write("rsub", 3);
		}
		if (subs.popf) {
			writer.comment("Subroutine: popf");
			writer.write("_popf lda _top", 3);
			writer.write("sub #6", 3);
			writer.write("sta _top", 3);
			writer.write("ldf @_top", 3);
			writer.write("rsub", 3);
		}
		if (subs.setlt) {
			writer.comment("Subroutine: setlt");
			writer.write("_setlt jlt * + 9", 3);
			writer.write("lda #0", 3);
			writer.write("j * + 6", 3);
			writer.write("lda #1", 3);
			writer.write("rsub", 3);
		}
		if (subs.setgt) {
			writer.comment("Subroutine: setgt");
			writer.write("_setgt jgt *+9", 3);
			writer.write("lda #0", 3);
			writer.write("j *+6", 3);
			writer.write("lda #1", 3);
			writer.write("rsub", 3);
		}
		if (subs.setlte) {
			writer.comment("Subroutine: setlte");
			writer.write("_setlte jgt *+9", 3);
			writer.write("lda #1", 3);
			writer.write("j *+6", 3);
			writer.write("lda #0", 3);
			writer.write("rsub", 3);
		}
		if (subs.setgte) {
			writer.comment("Subroutine: setgte");
			writer.write("_setgte jlt *+9", 3);
			writer.write("lda #1", 3);
			writer.write("j *+6", 3);
			writer.write("lda #0", 3);
			writer.write("rsub", 3);
		}
		if (subs.seteq) {
			writer.comment("Subroutine: seteq");
			writer.write("_seteq jeq * + 9", 3);
			writer.write("lda #0", 3);
			writer.write("j * + 6", 3);
			writer.write("lda #1", 3);
			writer.write("rsub", 3);
		}
		if (subs.setne) {
			writer.comment("Subroutine: setne");
			writer.write("_setne jeq * + 9", 3);
			writer.write("lda #1", 3);
			writer.write("j * + 6", 3);
			writer.write("lda #0", 3);
			writer.write("rsub", 3);
		}
		writer.write("base _frame", 0);
		writer.comment("BEGIN FUNCTION DECLARATIONS");
		writer.add(functions);
		writer.write("_data equ *", 0);
		writer.add(globals);
		writer.write("nobase", 0);
		writer.write("ltorg", 0);
		writer.comment("Program Entry (start)");
		writer.comment("Initialize stack top and frame");
		writer.write("_start ldb _top", 3);
		writer.write("J main", 4);
		writer.write("_frame equ * + 2048", 0);
		writer.write("end _start", 0);
		return writer.end();
	}
	
	public Block gen(FuncDeclaration d) {
		writer.start();
		writer.comment("Begin FuncDecl " + d.typeId + " " + d.id + "()");
		
		//Create param disps
		frame.incrementParam(d.getTotalParamSize());
		
		writer.comment("Push retaddr L onto stack");
		writer.write(String.format("%s rmo l,a", d.id), 2);
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
			writer.reserve(d.id, d.totalSize());
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
		return null;
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
		
		//Locals do not need to be ASM-decremented, since the body is forced to return
		if (size > 0) {
			writer.comment("Decrement locals");
			writer.decrement(size);
			frame.decrementLocal(size);
		}
		
		writer.comment("Leave Block");
		return writer.end();
	}
	
	public Block gen(ReturnStmt s) {
		writer.start();
		writer.comment("Begin Return");
		if (s.value != null) {
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
		writer.write("rmo a,l", 3);
		
		//Decrement params
		writer.comment("Decrement params");
		writer.decrement(frame.getParamDisp());
		
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
		Block neg = s.negative == null ? null : gen(s.negative);
		writer.add(cond);
		writer.write("comp #0", 3);
		//If-End
		if (neg == null) {
			writer.write("jeq * + " + (aff.size + 3), 3); //JEQ [END]
			writer.comment("Begin If Body");
			writer.add(aff);
		//If-Else-End
		} else {
			writer.write("jeq * + " + (aff.size + 6), 3); //JEQ [ELSE]
			writer.comment("Begin If Body");
			writer.add(aff);
			writer.write("j * + " + (neg.size + 3), 3); //J [END]
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
		throw new IllegalStateException();
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
			case FLOAT: writer.write("addf _fr", 3); break;
			case INT: writer.write("addr s,a", 2); break;
			case VOID:
			}
			break;
		case SUB: 
			switch (typeId) {
			case FLOAT: writer.write("subf _fr", 3); break;
			case INT: writer.write("subr s,a", 2); break;
			case VOID:
			}
			break;
		case MUL:
			switch (typeId) {
			case FLOAT: writer.write("mulf _fr", 3); break;
			case INT: writer.write("mulr s,a", 2); break;
			case VOID:
			}
			break;
		case DIV:
			switch (typeId) {
			case FLOAT: writer.write("divf _fr", 3); break;
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
		//TODO Jumps for relops??
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
		writer.write("lda _top", 3);
		writer.write("sub #" + e.func.getTotalParamSize(), 3);
		writer.write("rmo a,b", 2);
		writer.comment("Jump");
		writer.write("+JSUB " + e.func.id, 4);
		
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
			case GLOBAL: writer.write(String.format("lda #%s", var.id), 3); break;
			case LOCAL:
				writer.write(String.format("lda #_frame + %d", var.disp), 3);
				//writer.write("addr b,a", 2);
				break;
			case PARAM: writer.write(String.format("lda _frame + %d", var.disp), 3); break;
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
