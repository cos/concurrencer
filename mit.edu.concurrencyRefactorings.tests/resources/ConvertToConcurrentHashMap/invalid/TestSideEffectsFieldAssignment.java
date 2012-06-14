package invalid;

import java.util.HashMap;
import java.lang.String;

public class TestSideEffectsFieldAssignment{
	
	HashMap hm = new HashMap();
	int fieldF;
	
	public void doSideEffects() {
		if (hm.containsKey("a_key")) {
			String value = new String(" this is a value ");
			value.trim();
			fieldF = 12; //side-effect
			hm.put("a_key", value);
		}
	}
}