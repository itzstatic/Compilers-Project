package gen;

import java.io.PrintWriter;

public abstract class Block {
	public int size;
	
	public abstract void write(PrintWriter w);
}
