package ast;


public abstract class Node {
	public final Loc start;
	public final Loc end;
	
	public Node(Loc start, Loc end) {
		this.start = start;
		this.end = end;
	}
	
	public void accept(Visitor v) {
		v.visit(this);
		v.postVisit(this);
	}
}
