package object_in;

import java.util.HashMap;
import java.lang.String;

public class TestPutIfAbsentFormUsingContainsKey_WithCreateValue {
	
	HashMap map = new HashMap();
	
	void doPutIfAbsentBlockWithCreateValueOneStatement() {
		boolean keyExists = map.containsKey("a_key");
		if (!keyExists) {
			map.put("a_key", "aValue");
		}
	}

	void doPutIfAbsentBlockWithExtractCreateValue() {
		if (!map.containsKey("a_key")) {
			String aValue = null;
			if (map.isEmpty()) {
				aValue = "empty";
			} else { 
				aValue = "aValue";
			}
            map.put("a_key", aValue);
        }
	}
}