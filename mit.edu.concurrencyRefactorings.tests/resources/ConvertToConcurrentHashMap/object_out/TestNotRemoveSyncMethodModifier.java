package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TestNotRemoveSyncMethodProperty {
	
	ConcurrentHashMap hm = new ConcurrentHashMap();

	synchronized void doSyncModifier() {
		hm.put("a_key", "a_value");
		hm.put("another_key", "another_value");
	}
}