package gen;

import gen.block.ParentBlock;
import gen.block.SourceBlock;

import java.util.Stack;

import ast.TypeId;
import ast.node.Expr;
import ast.node.declare.VarDeclaration;

public class BlockWriter {
	
	private Stack<ParentBlock> parents = new Stack<>();
	
	private StringBuilder source = new StringBuilder();
	private int sourceSize; //Sum of all the write() calls until we flushed
	private int totalSize; //Sum of all the write() calls
	
	private StringBuilder prefix = new StringBuilder(); //indentation: number of tabs
	
	private Subroutines subs;
	
	public BlockWriter(Subroutines subs) {
		this.subs = subs;
	}
	
	public int size() {
		return totalSize;
	}
	
	public void start() {
		flush();
		prefix.append("\t");
		parents.push(new ParentBlock());
	}
	
	private void flush() {
		if (source.length() > 0) {
			parents.peek().add(new SourceBlock(source.toString(), sourceSize));
			source = new StringBuilder();
			sourceSize = 0;
		}
	}
	
	public Block end() {
		flush();
		prefix.deleteCharAt(prefix.length() - 1);
		return parents.pop();
	}
	
	public void add(Block block) {
		flush();
		if (block != null) {
			parents.peek().add(block);
		}
	}
	
	public void comment(String comment) {
		write("." + comment, 0);
	}
	
	public void write(String asm, int disp) {
		source.append(prefix);
		source.append(asm);
		source.append(System.lineSeparator());
		sourceSize += disp;
		totalSize += disp;
	}
	
	//Expects the index to be loaded in A
	//Puts index in X or ptr in @_ptr - whichever is necessary.
	public void index(VarDeclaration array) {
		write("mul #" + array.typeId.size, 3);
		switch(array.scope) {
		case GLOBAL:
		case LOCAL:
			write("rmo a,x", 2);
			break;
		case PARAM:
			write("add _frame + " + array.disp, 3);
			write("sta _ptr", 3);
			break;
		}
	}
	
	public void moveAccToReg(Expr e) {
		switch(e.getTypeId()) {
		case FLOAT: write("stf _fr", 3); break;
		case INT: write("rmo a,s", 2); break;
		case VOID:
		}
	}
	
	public void moveRegToAcc(Expr e) {
		switch(e.getTypeId()) {
		case FLOAT: write("ldf _fr", 3); break;
		case INT: write("rmo s,a", 2); break;
		case VOID:
		}
	}
	
	public void reserve(String label, int byteCount) {
		write(String.format("%s resb %d", label, byteCount), byteCount);
	}
	
	//Decrement top by A
	public void decrement(int n) {
		if (n == 0) {
			return;
		}
		write("lda _top", 3);
		write("sub #" + n, 3);
		write("sta _top", 3);
	}
	
	//Invalidates A
	public void increment(int n) {
		if (n == 0) {
			return;
		}
		write("lda _top", 3);
		write("add #" + n, 3);
		write("sta _top", 3);
	}
	
	//A into stack
	public void push(Expr e) {
		switch (e.getTypeId()) {
		case FLOAT: 
			subs.pushf = true;
			write("jsub _pushf" , 3); 
			break;
		case INT: 
			subs.push = true;
			write("jsub _push", 3); 
			break;
		case VOID: //Nothing
		}
	}
	
	//stack into A
	public void pop(Expr e) {
		switch (e.getTypeId()) {
		case FLOAT: 
			subs.popf = true;
			write("jsub _popf" , 3);
			break;
		case INT: 
			subs.pop = true;
			write("jsub _pop", 3); 
			break;
		case VOID: //Nothing
		}
	}
	
	public void compare(TypeId typeId) {
		switch(typeId) {
		case FLOAT: write("compf _fr", 3); break;
		case INT: write("compr a,s", 2); break;
		case VOID:
		}
	}
	
	public void seteq(TypeId typeId) {
		subs.seteq = true;
		compare(typeId);
		write("jsub _seteq", 3);
	}
	
	public void setne(TypeId typeId) {
		subs.setne = true;
		compare(typeId);
		write("jsub _setne", 3);
	}
	
	public void setlt(TypeId typeId) {
		subs.setlt = true;
		compare(typeId);
		write("jsub _setlt", 3);
	}
	
	public void setgt(TypeId typeId) {
		subs.setgt = true;
		compare(typeId);
		write("jsub _setgt", 3);
	}
	
	public void setlte(TypeId typeId) {
		subs.setlte = true;
		compare(typeId);
		write("jsub _setlte", 3);
	}
	
	public void setgte(TypeId typeId) {
		subs.setgte = true;
		compare(typeId);
		write("jsub _setgte", 3);
	}
	
	public void popWord() {
		subs.pop = true;
		write("jsub _pop", 3);
	}
	
	public void pushWord() {
		subs.push = true;
		write("jsub _push", 3);
	}
}
