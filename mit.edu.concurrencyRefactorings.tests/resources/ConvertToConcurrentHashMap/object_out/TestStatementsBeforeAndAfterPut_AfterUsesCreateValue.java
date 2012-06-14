package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.String;

public class TestStatementsBeforeAndAfterPut_AfterUsesCreateValue {
	
	ConcurrentHashMap hm = new ConcurrentHashMap();
	
	public void doBeforeAndAfterPut() {
		String createdvalue = createvalue();
		if (hm.putIfAbsent("a_key", createdvalue) == null) {
			value = createdvalue;
			System.out.println(value);
		}
	}

	private String createvalue() {
		String value = new String(" this is a value ");
		value = "";
		return value;
	}
}