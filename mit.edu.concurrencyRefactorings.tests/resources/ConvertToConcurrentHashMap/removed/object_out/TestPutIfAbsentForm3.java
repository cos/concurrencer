package object_in;

import java.util.HashMap;
import java.lang.String;

public class TestPutIfAbsentForm3 {
	
	HashMap hm = new HashMap();
	
	public String doPutIfAbsent() {
		return hm.putIfAbsent("a_key", "a_value");
	}
	
	public String doPutIfAbsent2() {
		return hm.putIfAbsent("a_key", "a_value");
	}
}