package gen;

import ast.node.Expr;
import ast.node.declare.VarDeclaration;

public class SicXeGenUtil {
	public static final int IMMEDIATE_MAX = 1 << 12 - 1;
	public static final int ABSOLUTE_MAX = 1 << 20 - 1;
	public static final int WORD_MAX = 1 << 24 - 1;
	
	//IEEE Masks to extract from double
	private static final long IEEE_SIGN = 1 << 63;
	private static final long IEEE_EXP = maskOfLowOnes(11) << 52;
	private static final long IEEE_MAN= maskOfLowOnes(52);
	
	//SicXe Masks to create a double
	private static final long SICXE_SGN = 1L << 47;
	private static final long SICXE_EXP = maskOfLowOnes(11) << 36;
	private static final long SICXE_MAN = maskOfLowOnes(36);
	
	private static long maskOfLowOnes(int n) {
		return ~(-1L << n);
	}
	
	public static long doubleToSicXeBits(double d) {
		long result = 0L;
		long bits = Double.doubleToLongBits(d);
		
		//Extract s, e, f from IEEE double and align to low
		long sgn = (IEEE_SIGN & bits) >>> 63;
		long exp = (IEEE_EXP & bits) >>> 52;
		long man = (IEEE_MAN & bits);
		
		//Convert s, e, f to SICXE equivalent
		//Sicxe is like .abcd whereas IEEE is like 1.abcd
		exp -= 1023; //Undo IEEE Bias
		exp++; //Account for the 1.
		man = (man >>> 1); //Make room for the 1.
		man += (1L << (52 - 1)); //Add the 1.
		exp += 1024; //Apply the Sicxe Bias
		man >>>= (52 - 36); //Because real numbers do place value backwards;
		//This shift is not for alignment.
		
		//Align and add to result
		result |= (SICXE_SGN & (sgn << 47));
		result |= (SICXE_EXP & (exp << 36));
		result |= (SICXE_MAN & man);
		return result;
	}
	
	public static void buildStoreA(StringBuilder store, Expr e) {
		switch(e.getTypeId()) {
		case FLOAT: store.append("stf "); break;
		case INT: store.append("sta "); break;
		case VOID:
		}
	}
	
	public static void buildLoadA(StringBuilder load, Expr e) {
		switch(e.getTypeId()) {
		case FLOAT: load.append("ldf "); break;
		case INT: load.append("lda "); break;
		case VOID:
		}
	}
	
	public static void buildPtr(StringBuilder instr, VarDeclaration array) {
		switch(array.scope){
		case GLOBAL: instr.append(String.format("%s,X", array.id)); break;
		case LOCAL: instr.append(String.format("_frame + %d,X", array.disp)); break;
		case PARAM: instr.append("@_ptr"); break;
		}
	}

	public static void buildVal(StringBuilder instr, VarDeclaration var) {
		switch(var.scope) {
		case GLOBAL: instr.append(var.id); break;
		case LOCAL:
		case PARAM: instr.append(String.format("_frame + %d", var.disp)); break;
		}
	}
	
}
