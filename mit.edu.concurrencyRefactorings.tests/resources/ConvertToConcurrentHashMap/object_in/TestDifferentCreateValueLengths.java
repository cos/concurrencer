package object_in;

import java.util.HashMap;

public class TestDifferentCreateValueLengths {
	
	private static HashMap _classCaches = new HashMap();

	public void doCreateValue() {
		Object obj = _classCaches.get("a_key");
		if (obj == null) {
			obj = new Object();
			_classCaches.put("a_key", obj);
		}
	}

	public void doCreateValue2() {
		Object obj = _classCaches.get("a_key");
		if (obj == null) {
			obj = new Object();
			int b = 2;
			_classCaches.put("a_key", obj);
		}
	}

	public void doCreateValue3() {
		Object obj = _classCaches.get("a_key");
		if (obj == null) {
			obj = new Object();
			int b = 2;
			_classCaches.put("a_key", obj);
			System.out.println(obj);
		}
	}

	public void doCreateValue4() {
		Object obj = _classCaches.get("a_key");
		if (obj == null) {
			obj = new Object();
			int b = 2;
			_classCaches.put("a_key", obj);
		}
		System.out.println(obj);
	}
	
	public void doCreateValue5() {
		Object obj = _classCaches.get("a_key");
		if (obj == null) {
			obj = new Object();
			_classCaches.put("a_key", obj);
			System.out.println(obj);
		}
	}

	public void doCreateValue6() {
		Object obj = _classCaches.get("a_key");
		if (obj == null) {
			obj = new Object();
			_classCaches.put("a_key", obj);
		}
		System.out.println(obj);
	}
}