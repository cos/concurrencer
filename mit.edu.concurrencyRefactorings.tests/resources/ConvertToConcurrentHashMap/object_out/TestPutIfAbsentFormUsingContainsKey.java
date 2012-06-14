package object_in;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.String;

public class TestPutIfAbsentFormUsingContainsKey {
	
	ConcurrentHashMap hm = new ConcurrentHashMap();
	
	void doPutIfAbsentBlockWithIntermediate() {
		hm.putIfAbsent("a_key", "a_value");
	}
	
	void doPutIfAbsentBlock() {
		hm.putIfAbsent("a_key", "a_value");
	}
	
	void doPutIfAbsentExpressionStatement() {
		hm.putIfAbsent("a_key", "a_value");
	}
}