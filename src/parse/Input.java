package parse;



import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.Stack;

//Buffered input backed by a Reader. It interprets instances of \r\n as \n
public class Input {
	
	public static final char EOS = (char)-1;
	
	private final int tabWidth;
	
	private PushbackReader reader;
	private int row = 1;
	private int col = 1;
	private int pos = 0;
	
	public Input(Reader reader, int maxLookahead, int tabWidth) {
		this.reader = new PushbackReader(reader, maxLookahead);
		this.tabWidth = tabWidth;
	}
	
	public int getRow() {
		return row;
	}
	public int getCol() {
		return col;
	}
	public int getPos() {
		return pos;
	}
	
	//Consumes the next character, but see class comment for 
	//special behavior for Microsoft line separator \r\n
	public char next() {
		try {
			char c = (char)reader.read();
			if (c == '\t') {
				col += tabWidth;
			} else {
				col++;
			}
			if (c == '\r') {
				c = (char)reader.read();
				if (c == '\n') {
					//Microsoft line terminator:
					c = '\n';
				} else {
					reader.unread(c);
				}
			}
			if (c == '\n' || c == '\r') {
				row++;
				col = 1;
			}
			pos++;
			return c;
		} catch (IOException e) {
			e.printStackTrace();
			return EOS;
		}
	}
	
	//Gets, but does not consume, the next character. See special
	//behavior for \r\n
	public char peek() {
		try {
			//Cache location, because next() could unpredictably change it
			int oldRow = row;
			int oldCol = col;
			int oldPos = pos;
			char c = next();
			reader.unread(c);
			row = oldRow;
			col = oldCol;
			pos = oldPos;
			return c;
		} catch (IOException e) {
			e.printStackTrace();
			return EOS;
		}
	}
	
	//Null if there are no digits
	public Integer readDigits() {
		StringBuilder builder = new StringBuilder();
		while (Character.isDigit(peek())) {
			builder.append(next());
		}
		if (builder.length() == 0) {
			return null;
		}
		return Integer.parseInt(builder.toString());
	}
	
	//Never returns null
	public String readLetters() {
		StringBuilder builder = new StringBuilder();
		while (Character.isLetter(peek())) {
			builder.append(next());
		}
		return builder.toString();
	}
	
	//Never returns null
	public String readLettersOrDigits() {
		StringBuilder builder = new StringBuilder();
		while (Character.isLetterOrDigit(peek())) {
			builder.append(next());
		}
		return builder.toString();
	}
	
	public boolean accept(char c) {
		if (peek() == c) {
			next();
			return true;
		}
		return false;
	}
	
	public boolean accept(String s) {
		int n = s.length();
		int oldRow = row;
		int oldCol = col;
		int oldPos = pos;
		Stack<Character> buffer = new Stack<>();
		char c;
		for (int i = 0; i < n; i++) {
			c = next();
			buffer.add(c);
			if (c != s.charAt(i)) {
				//Undo and cleanup
				row = oldRow;
				col = oldCol;
				pos = oldPos;
				//Push back onto the stream
				for (int j = 0; j <= i; j++) {
					try {
						reader.unread(buffer.pop());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return false;
			}
		}
		return true;
	}
	
	public void nextLine() {
		char c;
		while ((c = next()) != '\n' && c != '\r');
	}
}
