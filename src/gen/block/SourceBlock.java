package gen.block;

import gen.Block;

import java.io.PrintWriter;


public class SourceBlock extends Block {
	
	private String source;
	
	public SourceBlock(String source, int size) {
		this.source = source;
		this.size = size;
	}

	@Override
	public void write(PrintWriter w) {
		w.println(source);
	}
	
//	public void comment(String comment) {
//		write("." + comment, 0);
//	}
//	
//	public void write(String asm, int disp) {
//		source.append(asm);
//		source.append(System.lineSeparator());
//		size += disp;
//	}
//	
//	public void reserve(int byteCount) {
//		write("resb " + byteCount, byteCount);
//	}
//	
//	public void decrement(int n) {
//		write("lda #" + n, 3);
//		decrement();
//	}
//	
//	//Decrement top by A
//	public void decrement() {
//		write("lds top", 3);
//		write("subr a,s", 2);
//		write("sts top", 3);
//	}
//	
//	//Invalidates A
//	public void increment(int n) {
//		write("lda #" + n, 3);
//		write("lds top", 3);
//		write("addr a,s", 2);
//		write("sts top", 3);
//	}
//	
//	//A into stack
//	public void push() {
//		write("jsub push", 3);
//	}
//	
//	//stack into A
//	public void pop() {
//		write("jsub pop", 3);
//	}
//	
//	//frame = top
//	public void newframe() {
//		write("lda top", 3);
//		write("sta frame", 3);
//	}
}
