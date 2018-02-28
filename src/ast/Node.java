package ast;


public abstract class Node {
	public Location start;
	public Location end;
	
	public void accept(Visitor v) {
		v.visit(this);
		v.postVisit(this);
	}
}
