package parse;



import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import ast.Loc;
import ast.Node;

public class Logger {
	
	final public static Logger INSTANCE = new Logger();
	
	private Map<Loc, String> messages = new HashMap<>();
	
	public void log(Node node, String message) {
		log(node.start, node.end, message);
	}
	
	public void log(String message) {
		throw new CompileError(message);
		//System.err.println(message);
	}
	
	public void log(Loc loc, String message) {
		throw new CompileError(loc, loc + " " + message);
		//System.err.println(loc + " " + message);
	}
	
	public void log(Loc start, Loc end, String message) {
		throw new CompileError(start, end, start + " to " + end + " " + message);
		//System.err.println(start + " to " + end + " " + message);
	}
	
	public void print(PrintWriter w) {
		for (Map.Entry<Loc, String> pair : messages.entrySet()) {
			w.println(pair.getKey() + ": " + pair.getValue());
		}
	}
}
