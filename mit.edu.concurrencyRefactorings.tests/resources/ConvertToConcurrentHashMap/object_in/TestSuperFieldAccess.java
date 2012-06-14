package object_in;

import java.util.HashMap;

public class TestSuperFieldAccess {

	HashMap hm;
}

class SuperFieldAccess extends TestSuperFieldAccess {

	public void doSomething() {
		super.hm = new HashMap();
	}
}