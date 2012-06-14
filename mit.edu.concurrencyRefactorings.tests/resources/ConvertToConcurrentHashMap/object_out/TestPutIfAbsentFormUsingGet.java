package object_in;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.String;

public class TestPutIfAbsentFormUsingGet {
	
	ConcurrentHashMap hm = new ConcurrentHashMap();
	
	void doPutIfAbsent() {
		hm.putIfAbsent("a_key", "a_value");
	}
	
	void doPutIfAbsent2() {
		hm.putIfAbsent("a_key", "a_value");
	}
}