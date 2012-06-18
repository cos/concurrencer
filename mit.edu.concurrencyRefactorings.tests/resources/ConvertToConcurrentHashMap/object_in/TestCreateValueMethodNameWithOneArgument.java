package object_in;

import java.util.HashMap;

public class TestCreateValueMethodNameWithOneArgument {
	HashMap map2 = new HashMap();
	
	void doPutIfAbsentBlockWithExtractCreateValue() {
		String something = null;
		if (!map2.containsKey("a_key")) {
			String aValue = null;
			if (map2.isEmpty()) {
				aValue = something;
			} else { 
				aValue = "aValue";
			}
            map2.put("a_key", aValue);
        }
	}
}