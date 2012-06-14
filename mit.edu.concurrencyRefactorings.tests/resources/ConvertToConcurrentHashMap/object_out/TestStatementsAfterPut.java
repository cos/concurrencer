package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.String;

public class TestStatementsAfterPut {
	
	ConcurrentHashMap hm = new ConcurrentHashMap();
	
	public void doAfterPut() {
		if (hm.putIfAbsent("a_key", "a_value") == null) {
			int b = 2;
			System.out.println("a_key");
		}
	}
	
	public void doAfterPut2() {
		if (hm.putIfAbsent("a_key", "a_value") == null) {
			int c = 2;
			System.out.println("a_value");
		}
	}
}