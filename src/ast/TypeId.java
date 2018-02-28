package ast;


public enum TypeId {
	INT(3), //TODO Actual offsets?
	FLOAT(6),
	VOID(0);
	
	public final int size;
	
	private TypeId(int size) {
		this.size = size;
	}
	
	//Returns the max (i.e. promotion)
//	public static TypeId max(TypeId a, TypeId b) {
//		if (a.rank > b.rank) {
//			return a;
//		}
//		return b;
//	}
}
