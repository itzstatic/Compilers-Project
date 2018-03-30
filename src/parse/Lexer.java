package parse;



import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import ast.Loc;
import ast.Logger;

public class Lexer {
	
	private static final List<String> KEYWORDS = Arrays.asList(new String[]{
		"else", "float", "if", "int", "return", "void", "while",
	});
	
	private static final List<String> SPECIALS = Arrays.asList(new String[]{
		"+", "-", "*", "/", "<", "<=", ">", ">=", "==", "!=", "=", 
		";", ",", "(", ")", "[", "]", "{", "}", 
	});
	
	static {
		//Sort specials by descending length, so that the lexer will attempt to 
		//match longer specials first
		SPECIALS.sort((s0, s1) -> -Integer.compare(s0.length(), s1.length()));
	}
	
	private Input input;
	
	private Loc start = new Loc(1, 1, 0); //Of the last token read;
	
	//If empty, then a call to next() will consume input and leave buffer empty; 
	//and a call to peek() will consume input then push to the buffer.
	//If non-empty, then next() will pop the buffer - but not consume input;
	//and peek() will peek the buffer and have no side effects.
	private Stack<Token> buffer = new Stack<>();
	private Stack<Loc> locs = new Stack<>();
	
	public Lexer(Input input) {
		this.input = input;
	}
	
	//Next token, or null if there is no next
	public Token next() {
		if (!buffer.isEmpty()) {
			start = locs.pop();
			return buffer.pop();
		}
		
		char c;
		int depth = 0; //Of block comment
		boolean closed; //Whether a comment just closed
		//Skip whitespace and comments
		do {
			c = input.peek();
			closed = false; //By default
			
			//Open block comment
			if (input.accept("/*")) {
				depth++;
			}
			//Ensure that depth is positive, because instead of interpreting unmatched "*/"
			//as fatally illegal, the sample output implies that it should be interpreted as
			//two special character tokens: * followed by /. Since */ Will (likely) not be valid
			//C-, this only matters for the output for project 1.
			//Close comment
			else if (depth > 0 && input.accept("*/")) {
				depth--;
				if (depth == 0) { 
					closed = true;
				}
			}
			
			//Line comment
			else if (depth == 0 && input.accept("//")) {
				input.nextLine();
				closed = true;
			}
			
			else if (Character.isWhitespace(c) || depth > 0) {
				input.next();
			}
			
		} while ((Character.isWhitespace(c) || depth > 0 || closed) && c != Input.EOS);
		
		//EOS before close
		if (depth > 0 && c == Input.EOS) {
			//TODO: save block comment start location
			Logger.INSTANCE.log(location(), "Expected closing comment not end of stream");
		}
		
		if (c == Input.EOS) {
			return null;
		}
		
		start = location();
		
		//ID or keyword
		if (Character.isLetter(c)) {
			String result = input.readLetters();
			if (KEYWORDS.contains(result)) {
				return new Token(result, TokenType.KEYWORD);
			}
			return new Token(result, TokenType.ID);
		}
		
		//Int or float
		if (Character.isDigit(c)) {
			//This token is a float iff d != null or e != null
			Integer i = null, d = null, e = null; //Integer, decimal, exponent
//			if (input.accept('0')) {
//				Radix radix = null;
//				if (input.accept('b')) {
//					radix = Radix.BIN;
//					i = input.readBinary();
//				} else if (input.accept('o')) {
//					radix = Radix.OCT;
//					i = input.readOctal();
//				} else if (input.accept('x')) {
//					radix = Radix.HEX;
//					i = input.readHex();
//				} else {
//					Logger.INSTANCE.log("Expected radix specifier not " + c); 
//				}
//				return new Token(new IntegerNum(i, radix), TokenType.INTEGER);
//			}
			//Not a custom-radix integer
			//Required main portion
			i = input.readDigits();
			if (i == null) { //Could not fit in int
				Logger.INSTANCE.log(location(), "Integer out of range");
			}
			Boolean sign = null; //Of the exponent
			
			//Optional decimal float portion
			if (input.accept('.')) {
				d = input.readDigits();
				if (d == null) {
					Logger.INSTANCE.log(location(), "Expected float digits");
				} 
			}
			//Optional exponent float portion
			if (input.accept('E')) {
				//Optional unary sign portion
				if (input.accept('+')) {
					sign = true;
				} else if (input.accept('-')) {
					sign = false;
				}
				//Required magnitude portion
				e = input.readDigits();
				if (e == null) {
					Logger.INSTANCE.log(location(), "Expected float exponent");
				}
			}
			//If this token is a float
			if (e != null || d != null) {
				return new Token(new FloatNum(i, d, sign, e), TokenType.FLOAT);
			}
			return new Token(Integer.valueOf(i), TokenType.INTEGER);
		}
		
		for (String special : SPECIALS) {
			if (input.accept(special)) {
				return new Token(special, TokenType.SPECIAL);
			}
		}
		return new Token(input.next() + input.readLettersOrDigits(), TokenType.ERROR);
	}
	
	public void prev(Token token, Loc start) {
		buffer.push(token);
		locs.push(start);
	}
	
	public Token peek() {
		if (buffer.isEmpty()) {
			Loc start = this.start;
			buffer.push(next());
			locs.push(this.start);
			this.start = start;
		}
		return buffer.peek();
	}
	
	public void expectKeyword(String w) {
		if (!acceptKeyword(w)) {
			Token t = peek();
			if (t == null) {
				Logger.INSTANCE.log(start, location(), "Expected " + w + " not end of stream");
			} else {
				Logger.INSTANCE.log(start, location(), "Expected " + w + " not " + t.payload);
			}
		}
	}
	
	public boolean acceptKeyword(String w) {
		Token t = peek();
		if (t != null && t.type == TokenType.KEYWORD && ((String)t.payload).equals(w)) {
			next();
			return true;
		}
		return false;
	}
	
	public String expectId() {
		String id = acceptId();
		if (id == null) {
			Token t = peek();
			if (t == null) {
				Logger.INSTANCE.log(start, location(), "Expected ID not end of stream");
			} else {
				Logger.INSTANCE.log(start, location(), "Expected ID not " + t.payload);
			}
		}
		return id;
	}
	
	public String acceptId() {
		Token t = peek();
		if (t != null && t.type == TokenType.ID) {
			next();
			return (String)t.payload;
		}
		return null;
	}
	
	public boolean accept(String s) {
		Token t = peek();
		if (t != null && t.type == TokenType.SPECIAL && ((String)t.payload).equals(s)) {
			next();
			return true;
		}
		return false;
	}
	
	public void expect(String s) {
		if (!accept(s)) {
			Token t = peek();
			if (t == null) {
				Logger.INSTANCE.log(start, location(), "Expected " + s + " not end of stream");
			} else {
				Logger.INSTANCE.log(start, location(), "Expected " + s + " not " + t.payload);
			}
		}
	}
	
	public int expectInteger() {
		Integer result = acceptInteger();
		if (result == null) {
			Token t = peek();
			if (t == null) {
				Logger.INSTANCE.log(start, location(), "Expected integer not end of stream");
			} else {
				Logger.INSTANCE.log(start, location(), "Expected integer not " + t.payload);
			}
		}
		return result;
	}
	
	public Integer acceptInteger() {
		Token t = peek();
		if (t == null || t.type != TokenType.INTEGER) {
			return null;
		}
		next();
		return (Integer)t.payload;
	}
	
	public FloatNum acceptFloat() {
		Token t = peek();
		if (t == null || t.type != TokenType.FLOAT) {
			return null;
		}
		next();
		return (FloatNum)t.payload;
	}
	
	public int getRow() {
		return input.getRow();
	}
	public int getCol() {
		return input.getCol();
	}
	
	public Loc location() {
		return new Loc(getRow(), getCol(), input.getPos());
	}
	
	public Loc start() {
		return start;
	}
}
