




public class Token {
	public final TokenType type;
	//Error tokens have undefined payload;
	//Identifier tokens have a String;
	//Integer tokens have a boxed Integer;
	//Float tokens have a FloatNum;
	//Special tokens have a String;
	//Keyword tokens also have a String
	public final Object payload;
	
	public Token(Object payload, TokenType type) {
		this.payload = payload;
		this.type = type;
	}
}
