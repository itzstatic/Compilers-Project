package ast;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class Visitor {
	public void visit(Object o) {
		invoke("visit", o);
	}
	
	public void postVisit(Object o) {
		invoke("postVisit", o);
	}
	
	private void invoke(String methodName, Object o) {
		try {
			Method m = getClass().getMethod(methodName, o.getClass());
			m.invoke(this, o);
		} catch (SecurityException 
				| IllegalAccessException | IllegalArgumentException 
				| InvocationTargetException e) {
			Throwable t = e.getCause();	
			if (t instanceof CompileError) {
				throw (CompileError) t;	
			} else {
				t.printStackTrace();
				throw new RuntimeException(t);
			}
		} catch (NoSuchMethodException e) {
			//System.out.println(o.getClass().getSimpleName() + " no postvisit");
		}
	}
}
