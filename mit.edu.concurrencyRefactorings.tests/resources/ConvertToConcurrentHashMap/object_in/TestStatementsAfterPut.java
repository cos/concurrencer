package object_in;

import java.util.HashMap;
import java.lang.String;

public class TestStatementsAfterPut {
	
	HashMap hm = new HashMap();
	
	public void doAfterPut() {
		if (hm.containsKey("a_key")) {
			hm.put("a_key", "a_value");
			int b = 2;
			System.out.println("a_key");
		}
	}
	
	public void doAfterPut2() {
		Object result = hm.get("a_key");
		if (result == null) {
			hm.put("a_key", "a_value");
			int c = 2;
			System.out.println("a_value");
		}
	}
}