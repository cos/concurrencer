package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TestParenthesizedExpressionUnraveling {
	
	ConcurrentHashMap hm = new ConcurrentHashMap();

	void doParen() {
		hm.putIfAbsent("a_key", "a_value");
	}
	
	void doParen2() {
		hm.putIfAbsent("a_key", "a_value");
	}
	
	void doParen3() {
		hm.putIfAbsent("a_key", "a_value");
	}
}