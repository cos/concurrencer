package object_in;

import java.util.HashMap;

public class TestReturnType {
	
	HashMap hm = new HashMap();
	HashMap hm2 = new HashMap();

	public HashMap doReturn() {
		return hm;
	}
	
	public HashMap doReturn2() {
		return hm2;
	}
	
	public Map doReturn3() {
		return hm;
	}
}