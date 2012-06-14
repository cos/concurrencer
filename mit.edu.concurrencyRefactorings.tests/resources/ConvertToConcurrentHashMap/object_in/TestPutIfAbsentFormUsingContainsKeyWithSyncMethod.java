package object_in;

import java.util.HashMap;
import java.lang.String;

public class TestPutIfAbsentFormUsingContainsKeyWithSyncMethod {
	
	HashMap hm = new HashMap();
	
	synchronized void doPutIfAbsent() {
		boolean keyExists = hm.containsKey("a_key");
		if (!keyExists) {
			hm.put("a_key", "a_value");
		}
	}
	
	synchronized void doPutIfAbsent2() {
		if (!hm.containsKey("a_key")) {
            hm.put("a_key", "a_value");
        }
	}
	
	synchronized void doPutIfAbsent3() {
		String something = "";
		if (!hm.containsKey("a_key")) {
            hm.put("a_key", "a_value");
        }
	}
}