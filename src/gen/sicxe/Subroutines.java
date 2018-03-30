package gen.sicxe;

public class Subroutines {
	public boolean push, pop, pushf, popf;
	public boolean setlt, setgt, seteq, setne, setlte, setgte;
	
	//If any of the sets are true
	public boolean set() {
		return setlt || setgt || seteq || setne || setlte || setgte;
	}
	
	public void all() {
		push = pop = pushf = popf = true;
		setlt = setgt = seteq = setne = setlte = setgte = true;
	}
}
