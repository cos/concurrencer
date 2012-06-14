package object_in;

import java.util.HashMap;

public class TestReplaceFormWithValueTypeReturn {
	
	HashMap hm = new HashMap();
	
	public void doReplace() {
		Object result = hm.get("a_key");
		if(result != null)
			hm.put("a_key", "a_value");
	}
	
	public void doReplace2() {
		if(hm.get("a_key") != null)
			hm.put("a_key", "a_value");
	}
	
	public void doReplace3() {
		boolean result = hm.containsKey("a_key");
		if(result)
			hm.put("a_key", "a_value");
	}
	
	public void doReplace4() {
		if(hm.containsKey("a_key"))
			hm.put("a_key", "a_value");
	}
}