package object_in;

import java.util.HashMap;

public class TestNotRemoveSyncMethodProperty {
	
	HashMap hm = new HashMap();

	synchronized void doSyncModifier() {
		hm.put("a_key", "a_value");
		hm.put("another_key", "another_value");
	}
}