package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TestInitializeFieldAccesses {
	
	ConcurrentHashMap hm;
	
	public void init() {
		hm = new ConcurrentHashMap();
		this.hm = new ConcurrentHashMap();
		new TestInitializeFieldAccesses().hm = new ConcurrentHashMap();
	}
}
