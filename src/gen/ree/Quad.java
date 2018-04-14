package gen.ree;

public class Quad {
	public final Mnemonic mnemonic;
	public final String op1;
	public final String op2;
	public String result;
	
	public Quad(Mnemonic mnemonic, String op1, String op2, String result) {
		this.mnemonic = mnemonic;
		this.op1 = op1 == null ? "" : op1;
		this.op2 = op2 == null ? "" : op2;
		this.result = result == null ? "" : result;
	}
	
	public Quad(Mnemonic mnemonic) {
		this(mnemonic, "", "", "");
	}
	
	public Quad(Mnemonic mnemonic, String op1) {
		this(mnemonic, op1, "", "");
	}
}
