package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TestRemoveFormWithValueTypeReturn {
	
	ConcurrentHashMap hm = new ConcurrentHashMap();
	
	public void doRemove() {
		hm.remove("a_key");
	}
	
	public void doRemove2() {
		hm.remove("a_key");
	}
	
	public void doRemove3() {
		hm.remove("a_key");
	}
	
	public void doRemove4() {
		hm.remove("a_key");
	}
}