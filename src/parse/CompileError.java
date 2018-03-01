package parse;

import ast.Location;



public class CompileError extends RuntimeException {
	
	private static final long serialVersionUID = -4505248331882449962L;
	
	public final Location start;
	public final Location end;
	
	public CompileError(String message) {
		super(message);
		start = null;
		end = null;
	}
	
	public CompileError(Location loc, String message) {
		super(message);
		start = loc;
		end = null;
	}
	
	public CompileError(Location start, Location end, String message) {
		super(message);
		this.start = start;
		this.end = end;
	}
}
