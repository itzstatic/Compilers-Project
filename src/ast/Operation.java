package ast;

public enum Operation {
	//Arithmetic
	ADD(false),
	SUB(false),
	MUL(false),
	DIV(false),
	//Relational
	LT(true),
	GT(true),
	LTE(true),
	GTE(true),
	NE(true),
	EQ(true);
	
	private boolean relational;
	
	//Otherwise, it is arithmetic
	private Operation(boolean relational) {
		this.relational = relational;
	}
	
	public boolean isRelational() {
		return relational;
	}
}
