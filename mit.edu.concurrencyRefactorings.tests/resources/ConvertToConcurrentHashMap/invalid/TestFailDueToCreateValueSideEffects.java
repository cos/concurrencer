package invalid;

import java.util.HashMap;
import java.lang.String;

public class TestFailDueToCreateValueSideEffects {
	
	HashMap hm = new HashMap();

	private HashMap doSomething() {
		return null;
	}
	
	public void doSideEffects() {
		if (hm.containsKey("a_key")) {
			String value = new String(" this is a value ");
			value.trim();
			hm = doSomething(); // side-effect
			hm = new HashMap(); // side-effect
			hm.clear();			// side-effect
			hm.put("a_key", value);
		}
	}
}