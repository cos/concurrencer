package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TestRemoveFormWithBooleanReturn {
	
	ConcurrentHashMap hm = new ConcurrentHashMap();
	
	public void doRemove() {
		hm.remove("a_key", "a_value");
	}
	
	public void doRemove2() {
		hm.remove("a_key", "a_value");
	}
}