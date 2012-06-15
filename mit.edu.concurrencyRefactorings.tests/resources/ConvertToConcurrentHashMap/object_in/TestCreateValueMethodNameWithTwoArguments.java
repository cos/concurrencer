package object_in;

import java.util.HashMap;

public class TestCreateValueMethodNameWithTwoArguments {
	HashMap map2 = new HashMap();
	
	void doPutIfAbsentBlockWithExtractCreateValue() {
		String something = null;
		String another = null;
		if (!map2.containsKey("a_key")) {
			String aValue = null;
			if (map2.isEmpty()) {
				aValue = something;
			} else { 
				aValue = another;
			}
            map2.put("a_key", aValue);
        }
	}
}