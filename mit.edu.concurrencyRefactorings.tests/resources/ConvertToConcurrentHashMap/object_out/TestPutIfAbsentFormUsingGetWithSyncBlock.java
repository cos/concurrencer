package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.String;

public class TestPutIfAbsentFormUsingGetWithSyncBlock {

	ConcurrentHashMap hm = new ConcurrentHashMap();

	void doPutIfAbsent() {
		hm.putIfAbsent("a_key", "a_value");
	}

	void doPutIfAbsent2() {
		hm.putIfAbsent("a_key", "a_value");
	}
	
	void doPutIfAbsent3() {
		synchronized (this) {
			String something = "";
			hm.putIfAbsent("a_key", "a_value");
		}
	}
}