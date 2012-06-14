package object_in;

import java.util.HashMap;

public class TestRemoveSyncBlockWithSameLock {
	
	HashMap hm = new HashMap();

	void doSyncBlock() {
		synchronized(hm) {
			hm.put("a_key", "a_value");
		}
	}
}