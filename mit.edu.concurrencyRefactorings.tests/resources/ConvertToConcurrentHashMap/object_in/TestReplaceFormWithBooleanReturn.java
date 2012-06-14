package object_in;

import java.util.HashMap;

public class TestReplaceFormWithBooleanReturn {
	
	HashMap hm = new HashMap();
	
	public void doReplace() {
		boolean result = hm.get("a_key").equals("old_value");
		if (result)
			hm.put("a_key", "new_value");
	}
	
	public void doReplace2() {
		if (hm.get("a_key").equals("old_value"))
			hm.put("a_key", "new_value");
	}
}