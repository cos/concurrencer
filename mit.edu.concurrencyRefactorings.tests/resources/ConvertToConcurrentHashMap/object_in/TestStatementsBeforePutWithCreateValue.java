package object_in;

import java.util.HashMap;
import java.lang.String;

public class TestStatementsBeforePutWithCreateValue {
	
	HashMap hm = new HashMap();

	public void doBeforePut() {
		if (hm.containsKey("a_key")) {
			String value = new String(" this is a value ");
			value = "";
			hm.put("a_key", value);
		}
	}
}