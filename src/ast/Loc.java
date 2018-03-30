package ast;

public class Loc {
	final public int row;
	final public int col;
	final public int pos;
	
	public Loc(int row, int col, int pos) {
		this.row = row;
		this.col = col;
		this.pos = pos;
	}
	
	@Override
	public int hashCode() {
		int code = 13;
		code ^= Integer.hashCode(row);
		code ^= Integer.hashCode(col);
		return code;
	}
	
	@Override
	public String toString() {
		return "R: " + row + " C: " + col;
	}
}
