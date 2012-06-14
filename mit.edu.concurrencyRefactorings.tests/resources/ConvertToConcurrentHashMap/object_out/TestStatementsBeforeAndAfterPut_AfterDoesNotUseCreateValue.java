package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.String;

public class TestStatementsBeforeAndAfterPut_AfterDoesNotUseCreateValue {
	
	ConcurrentHashMap hm = new ConcurrentHashMap();
	
	public void doBeforeAndAfterPut() {
		if (hm.putIfAbsent("a_key", createvalue()) == null) {
			System.out.println("a_key");
		}
	}

	private String createvalue() {
		String value = new String(" this is a value ");
		value = "";
		return value;
	}
}