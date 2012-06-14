package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TestReplaceFormWithValueTypeReturn {
	
	ConcurrentHashMap hm = new ConcurrentHashMap();
	
	public void doReplace() {
		hm.replace("a_key", "a_value");
	}
	
	public void doReplace2() {
		hm.replace("a_key", "a_value");
	}
	
	public void doReplace3() {
		hm.replace("a_key", "a_value");
	}
	
	public void doReplace4() {
		hm.replace("a_key", "a_value");
	}
}