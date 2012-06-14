package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TestRemoveSyncMethodProperty {
	
	ConcurrentHashMap hm = new ConcurrentHashMap();

	void doSyncModifier() {
		hm.put("a_key", "a_value");
	}
}