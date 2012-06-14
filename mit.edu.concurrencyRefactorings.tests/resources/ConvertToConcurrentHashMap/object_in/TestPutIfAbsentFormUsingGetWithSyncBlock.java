package object_in;

import java.util.HashMap;
import java.lang.String;

public class TestPutIfAbsentFormUsingGetWithSyncBlock {

	HashMap hm = new HashMap();

	void doPutIfAbsent() {
		synchronized (this) {
			Object result = hm.get("a_key");
			if (result == null) {
				hm.put("a_key", "a_value");
			}
		}
	}

	void doPutIfAbsent2() {
		synchronized (this) {
			if (hm.get("a_key") == null) {
				hm.put("a_key", "a_value");
			}
		}
	}
	
	void doPutIfAbsent3() {
		synchronized (this) {
			String something = "";
			if (hm.get("a_key") == null) {
				hm.put("a_key", "a_value");
			}
		}
	}
}