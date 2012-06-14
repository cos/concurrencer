package object_in;

import java.util.HashMap;

public class TestAddItem {
	
	HashMap hm = new HashMap();

	void doCommonMethods() {
		hm.clear();
		hm.containsKey("a_key");
		hm.containsValue("a_value");
		hm.entrySet();
		hm.get("a_key");
		hm.isEmpty();
		hm.keySet();
		hm.put("a_key","a_value");
		hm.putAll(new HashMap());
		hm.remove("a_key");
		hm.size();
		hm.values();
	}
}