package object_in;

import java.util.HashMap;
import java.lang.String;

public class TestPutIfAbsentFormUsingContainsKeyWithSyncBlock {
	
	HashMap hm = new HashMap();
	
	void doPutIfAbsent() {
		synchronized(this) {
			boolean keyExists = hm.containsKey("a_key");
			if (!keyExists) {
				hm.put("a_key", "a_value");
			}
		}
	}
	
	void doPutIfAbsent2() {
		synchronized(this) {
			if (!hm.containsKey("a_key")) {
	            hm.put("a_key", "a_value");
	        }
		}
	}
	
	void doPutIfAbsent3() {
		synchronized(this) {
			String something = "";
			if (!hm.containsKey("a_key")) {
	            hm.put("a_key", "a_value");
	        }
		}
	}
}