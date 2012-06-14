package object_in;

import java.util.HashMap;
import java.lang.String;

public class TestPutIfAbsentFormUsingContainsKey {
	
	HashMap hm = new HashMap();
	
	void doPutIfAbsentBlockWithIntermediate() {
		boolean keyExists = hm.containsKey("a_key");
		if (!keyExists) {
			hm.put("a_key", "a_value");
		}
	}
	
	void doPutIfAbsentBlock() {
		if (!hm.containsKey("a_key")) {
            hm.put("a_key", "a_value");
        }
	}
	
	void doPutIfAbsentExpressionStatement() {
		if (!hm.containsKey("a_key"))
            hm.put("a_key", "a_value");
	}
}