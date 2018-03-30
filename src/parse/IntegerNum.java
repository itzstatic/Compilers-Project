package parse;

public class IntegerNum {
	
	private int value;
	private Radix radix;
	
	public IntegerNum(int value, Radix radix) {
		this.value = value;
		this.radix = radix;
	}
	
	public int intValue() {
		return value;
	}
	
	@Override
	public String toString() {
		switch(radix) {
		case DEC: return Integer.toString(value);
		case BIN: return Integer.toBinaryString(value);
		case OCT: return Integer.toOctalString(value);
		case HEX: return Integer.toHexString(value);
		default: throw new IllegalStateException();
		}
	}
}
