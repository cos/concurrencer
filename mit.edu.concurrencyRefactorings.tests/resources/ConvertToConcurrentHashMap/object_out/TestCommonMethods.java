package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TestAddItem {
	
	ConcurrentHashMap hm = new ConcurrentHashMap();

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