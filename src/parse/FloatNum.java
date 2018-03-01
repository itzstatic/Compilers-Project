package parse;


public class FloatNum {
	private int left;
	//Null indicates there is no decimal (123E-4)
	private Integer right;
	//Null indicates there is no sign (123E4)
	private Boolean sign; //Of the exponent (positive sign is true)
	//Null indicates there is no exponent (123.4);
	private Integer exp;
	
	public FloatNum(int left, Integer right, Boolean sign, Integer exp) {
		this.left = left;
		this.right = right;
		this.sign = sign;
		this.exp = exp;
	}
	
	public double doubleValue() {
		return Float.parseFloat(toString());
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(left);
		if (right != null) {
			result.append('.');
			result.append(right);
		}
		if (exp != null) {
			result.append('E');
			if (sign != null) {
				result.append(sign.booleanValue() ? '+' : '-');
			}
			result.append(exp);
		}
		return result.toString();
	}
}
