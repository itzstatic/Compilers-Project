package gen;

public class Frame {
	
	private int paramSize;
	private int localSize;
	
	public void incrementParam(int byteCount) {
		paramSize += byteCount;
	}
	
	public void incrementLocal(int byteCount) {
		localSize += byteCount;
	}
	
	public void decrementLocal(int byteCount) {
		localSize -= byteCount;
	}
	
	public void decrementAllLocals() {
		localSize = 0;
	}
	
//	public void reset() {
//		localSize = 0;
//		paramSize = 0;
//	}
	
	public int getFrameDisp() {
		return 3 + paramSize + localSize;
	}
	
	public int getLocalDisp() {
		return localSize;
	}
	
	public int getParamDisp() {
		return paramSize;
	}

	public void decrementAllParams() {
		paramSize = 0;
	}
}
