package object_in;

import java.util.HashMap;

public class TestInitializeFieldAccesses {
	
	HashMap hm;
	
	public void init() {
		hm = new HashMap();
		this.hm = new HashMap();
		new TestInitializeFieldAccesses().hm = new HashMap();
	}
}
