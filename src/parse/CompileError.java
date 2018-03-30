package parse;

import ast.Loc;



public class CompileError extends RuntimeException {
	
	private static final long serialVersionUID = -4505248331882449962L;
	
	public final Loc start;
	public final Loc end;
	
	public CompileError(String message) {
		super(message);
		start = null;
		end = null;
	}
	
	public CompileError(Loc loc, String message) {
		super(message);
		start = loc;
		end = null;
	}
	
	public CompileError(Loc start, Loc end, String message) {
		super(message);
		this.start = start;
		this.end = end;
	}
}
