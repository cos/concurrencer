package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TestRemoveSyncBlockWithThisLock {
	
	ConcurrentHashMap hm = new ConcurrentHashMap();

	void doSyncBlock() {
		hm.put("a_key", "a_value");
	}
}