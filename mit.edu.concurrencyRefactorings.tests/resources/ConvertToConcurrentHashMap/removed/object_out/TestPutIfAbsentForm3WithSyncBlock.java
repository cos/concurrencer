package object_out;

import java.util.HashMap;
import java.lang.String;
import java.util.concurrent.ConcurrentHashMap;

public class TestPutIfAbsentForm3WithSyncBlock {
	
	ConcurrentHashMap hm = new ConcurrentHashMap();
	
	public String doPutIfAbsent() {
		return hm.putIfAbsent("a_key", "a_value");
	}
	
	public String doPutIfAbsent2() {
		return hm.putIfAbsent("a_key", "a_value");
	}
}