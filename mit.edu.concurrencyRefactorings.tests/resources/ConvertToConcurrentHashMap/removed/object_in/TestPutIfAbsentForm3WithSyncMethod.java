package object_in;

import java.util.HashMap;
import java.lang.String;

public class TestPutIfAbsentForm3WithSyncMethod {
	
	HashMap hm = new HashMap();
	
	public synchronized String doPutIfAbsent() {
		boolean result = hm.containsKey("a_key"); 
		if (!result) 
			return hm.put("a_key", "a_value");
		else
			return hm.get("a_key");

	}
	
	public synchronized String doPutIfAbsent2() {
		if (!hm.containsKey("a_key")) 
			return hm.put("a_key", "a_value");
		else
			return hm.get("a_key");
	}
}