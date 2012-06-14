package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TestReplaceFormWithBooleanReturn {
	
	ConcurrentHashMap hm = new ConcurrentHashMap();
	
	public void doReplace() {
		hm.replace("a_key", "old_value", "new_value");
	}
	
	public void doReplace2() {
		hm.replace("a_key", "old_value", "new_value");
	}
}