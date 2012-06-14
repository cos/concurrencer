package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TestNotRemoveSyncBlockWithSameLock {
	
	ConcurrentHashMap hm = new ConcurrentHashMap();

	void doSyncBlock() {
		synchronized(hm) {
			hm.put("a_key", "a_value");
			hm.put("another_key", "another_value");
		}
	}
}