package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.String;

public class TestStatementsBeforePutWithCreateValue {
	
	ConcurrentHashMap hm = new ConcurrentHashMap();

	public void doBeforePut() {
		hm.putIfAbsent("a_key", createvalue());
	}

	private String createvalue() {
		String value = new String(" this is a value ");
		value = "";
		return value;
	}
}