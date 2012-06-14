package object_in;

import java.util.HashMap;

public class TestParenthesizedExpressionUnraveling {
	
	HashMap hm = new HashMap();

	void doParen() {
		if (!(hm.containsKey("a_key"))) {
            hm.put("a_key", "a_value");
        }
	}
	
	void doParen2() {
		if (!((hm.containsKey("a_key")))) {
            hm.put("a_key", "a_value");
        }
	}
	
	void doParen3() {
		if (!(((hm.containsKey("a_key"))))) {
            hm.put("a_key", "a_value");
        }
	}
}