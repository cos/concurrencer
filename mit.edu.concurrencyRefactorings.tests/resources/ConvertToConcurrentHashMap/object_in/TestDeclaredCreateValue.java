package object_in;

import java.util.Map;
import java.util.HashMap;
import java.lang.String;

public class TestDeclaredCreateValue {
	
	HashMap map = new HashMap();

	void doPutIfAbsentBlockWithCreateValueOneStatement(String hi, HashMap<String, Integer> hello) {
		boolean keyExists = map.containsKey("a_key");
		if (!keyExists) {
			String aValue = "aValue!";
			map.put("a_key", aValue);
		}
	}
}