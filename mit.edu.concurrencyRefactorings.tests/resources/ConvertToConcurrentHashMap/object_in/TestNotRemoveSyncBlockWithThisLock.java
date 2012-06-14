package object_in;

import java.util.HashMap;

public class TestNotRemoveSyncBlockWithThisLock {
	
	HashMap hm = new HashMap();

	void doSyncBlock() {
		synchronized(this) {
			hm.put("a_key", "a_value");
			hm.put("another_key", "another_value");
		}
	}
}