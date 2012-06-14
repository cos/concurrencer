package object_in;

import java.util.HashMap;
import java.lang.String;

public class TestPutIfAbsentFormUsingGet {
	
	HashMap hm = new HashMap();
	
	void doPutIfAbsent() {
		Object result = hm.get("a_key");
        if (result == null) {
            hm.put("a_key", "a_value");
        }
	}
	
	void doPutIfAbsent2() {
		if (hm.get("a_key") == null) {
			hm.put("a_key", "a_value");
		}
	}
}