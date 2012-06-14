package object_in;

import java.util.HashMap;

public class TestRemoveFormWithBooleanReturn {
	
	HashMap hm = new HashMap();
	
	public void doRemove() {
		boolean result = hm.get("a_key").equals("a_value");
		if(result)
			hm.remove("a_key");
	}
	
	public void doRemove2() {
		if(hm.get("a_key").equals("a_value"))
			hm.remove("a_key");
	}
}