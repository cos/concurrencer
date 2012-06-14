package object_in;

import java.util.HashMap;

public class TestNotRemoveSyncBlockWithSameLock {
	
	HashMap hm = new HashMap();

	void doSyncBlock() {
		synchronized(hm) {
			hm.put("a_key", "a_value");
			hm.put("another_key", "another_value");
		}
	}
}