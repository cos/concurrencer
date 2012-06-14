package object_in;

import java.util.HashMap;

public class TestRemoveSyncMethodProperty {
	
	HashMap hm = new HashMap();

	synchronized void doSyncModifier() {
		hm.put("a_key", "a_value");
	}
}