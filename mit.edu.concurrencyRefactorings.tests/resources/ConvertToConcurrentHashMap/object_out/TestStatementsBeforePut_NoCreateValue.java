package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.String;

public class TestStatementsBeforePut_NoCreateValue {
	
	public ConcurrentHashMap hm = new ConcurrentHashMap();
	private static final String THE_KEY = "a_key";
	
	public void doBeforePutNoCreateValue() {
		if (hm.putIfAbsent(THE_KEY, "a_value") == null) {
			System.out.println(THE_KEY);
		}
	}
}