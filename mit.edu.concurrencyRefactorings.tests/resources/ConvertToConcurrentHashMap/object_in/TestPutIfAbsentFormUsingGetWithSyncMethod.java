package object_in;

import java.util.HashMap;
import java.lang.String;

public class TestPutIfAbsentFormUsingGetWithSyncMethod {

	HashMap hm = new HashMap();

	synchronized void doPutIfAbsent() {
		Object result = hm.get("a_key");
		if (result == null) {
			hm.put("a_key", "a_value");
		}
	}

	synchronized void doPutIfAbsent2() {
		if (hm.get("a_key") == null) {
			hm.put("a_key", "a_value");
		}
	}

	synchronized void doPutIfAbsent3() {
		String something = "";
		if (hm.get("a_key") == null) {
			hm.put("a_key", "a_value");
		}
	}
}