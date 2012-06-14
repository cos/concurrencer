package object_in;

import java.util.HashMap;

public class TestRemoveFormWithValueTypeReturn {
	
	HashMap hm = new HashMap();
	
	public void doRemove() {
		Object result = hm.get("a_key");
		if(result != null)
			hm.remove("a_key");
	}
	
	public void doRemove2() {
		if(hm.get("a_key") != null)
			hm.remove("a_key");
	}
	
	public void doRemove3() {
		boolean result = hm.containsKey("a_key");
		if(result)
			hm.remove("a_key");
	}
	
	public void doRemove4() {
		if(hm.containsKey("a_key"))
			hm.remove("a_key");
	}
}