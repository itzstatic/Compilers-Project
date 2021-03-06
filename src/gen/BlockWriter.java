package gen;

import gen.block.ParentBlock;
import gen.block.SourceBlock;
import gen.sicxe.Subroutines;

import java.util.Stack;

import ast.Sizes;
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
	private Sizes sizes;
	
	public BlockWriter(Subroutines subs, Sizes sizes) {
		this.subs = subs;
		this.sizes = sizes;
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
		write("mul #" + sizes.sizeOf(array.typeId), 3);
		switch(array.scope) {
		case GLOBAL:
		case LOCAL:
			write("rmo a,x", 2);
			break;
		case PARAM:
			write("add frame + " + array.disp, 3);
			write("sta ptr", 3);
			break;
		}
	}
	
	public void moveAccToReg(Expr e) {
		switch(e.getTypeId()) {
		case FLOAT: write("stf fr", 3); break;
		case INT: write("rmo a,s", 2); break;
		case VOID:
		}
	}
	
	public void moveRegToAcc(Expr e) {
		switch(e.getTypeId()) {
		case FLOAT: write("ldf fr", 3); break;
		case INT: write("rmo s,a", 2); break;
		case VOID:
		}
	}
	
	//Decrement top by A
	public void decrement(int n) {
		if (n == 0) {
			return;
		}
		write("lda top", 3);
		write("sub #" + n, 3);
		write("sta top", 3);
	}
	
	//Invalidates A
	public void increment(int n) {
		if (n == 0) {
			return;
		}
		write("lda top", 3);
		write("add #" + n, 3);
		write("sta top", 3);
	}
	
	//A into stack
	public void push(Expr e) {
		switch (e.getTypeId()) {
		case FLOAT: 
			subs.pushf = true;
			write("jsub pushf" , 3); 
			break;
		case INT: 
			subs.push = true;
			write("jsub push", 3); 
			break;
		case VOID: //Nothing
		}
	}
	
	//stack into A
	public void pop(Expr e) {
		switch (e.getTypeId()) {
		case FLOAT: 
			subs.popf = true;
			write("jsub popf" , 3);
			break;
		case INT: 
			subs.pop = true;
			write("jsub pop", 3); 
			break;
		case VOID: //Nothing
		}
	}
	
	public void compare(TypeId typeId) {
		switch(typeId) {
		case FLOAT: write("compf fr", 3); break;
		case INT: write("compr a,s", 2); break;
		case VOID:
		}
	}
	
	public void seteq(TypeId typeId) {
		subs.seteq = true;
		compare(typeId);
		write("jsub seteq", 3);
	}
	
	public void setne(TypeId typeId) {
		subs.setne = true;
		compare(typeId);
		write("jsub setne", 3);
	}
	
	public void setlt(TypeId typeId) {
		subs.setlt = true;
		compare(typeId);
		write("jsub setlt", 3);
	}
	
	public void setgt(TypeId typeId) {
		subs.setgt = true;
		compare(typeId);
		write("jsub setgt", 3);
	}
	
	public void setlte(TypeId typeId) {
		subs.setlte = true;
		compare(typeId);
		write("jsub setlte", 3);
	}
	
	public void setgte(TypeId typeId) {
		subs.setgte = true;
		compare(typeId);
		write("jsub setgte", 3);
	}
	
	public void popWord() {
		subs.pop = true;
		write("jsub pop", 3);
	}
	
	public void pushWord() {
		subs.push = true;
		write("jsub push", 3);
	}
}
