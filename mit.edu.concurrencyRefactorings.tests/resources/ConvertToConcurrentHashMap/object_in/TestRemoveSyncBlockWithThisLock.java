package object_in;

import java.util.HashMap;

public class TestRemoveSyncBlockWithThisLock {
	
	HashMap hm = new HashMap();

	void doSyncBlock() {
		synchronized(this) {
			hm.put("a_key", "a_value");
		}
	}
}