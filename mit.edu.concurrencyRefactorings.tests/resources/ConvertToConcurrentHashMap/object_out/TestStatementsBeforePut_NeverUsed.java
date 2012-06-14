package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.String;

public class TestStatementsBeforePut_NeverUsed {
	
	ConcurrentHashMap hm = new ConcurrentHashMap();
	
	public void doBeforeAndAfterPut() {
		if (hm.putIfAbsent("a_key", "") == null) {
			String value = new String(" this is a value ");
		}
	}
}