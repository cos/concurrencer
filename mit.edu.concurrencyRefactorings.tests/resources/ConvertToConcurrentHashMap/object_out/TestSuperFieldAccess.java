package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TestSuperFieldAccess {

	ConcurrentHashMap hm;
}

class SuperFieldAccess extends TestSuperFieldAccess {

	public void doSomething() {
		super.hm = new ConcurrentHashMap();
	}
}