package object_out;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TestDifferentCreateValueLengths {
	
	private static ConcurrentHashMap _classCaches = new ConcurrentHashMap();

	public void doCreateValue() {
		_classCaches.putIfAbsent("a_key", new Object());
	}

	public void doCreateValue2() {
		Object obj = _classCaches.get("a_key");
		Object createdobj = createobj();
		if (_classCaches.putIfAbsent("a_key", createdobj) == null) {
			obj = createdobj;
		}
	}

	private Object createobj() {
		Object obj;
		obj = new Object();
		int b = 2;
		return obj;
	}

	public void doCreateValue3() {
		Object obj = _classCaches.get("a_key");
		Object createdobj = createobj();
		if (_classCaches.putIfAbsent("a_key", createdobj) == null) {
			obj = createdobj;
			System.out.println(obj);
		}
	}

	private Object createobj() {
		Object obj;
		obj = new Object();
		int b = 2;
		return obj;
	}

	public void doCreateValue4() {
		Object obj = _classCaches.get("a_key");
		Object createdobj = createobj();
		if (_classCaches.putIfAbsent("a_key", createdobj) == null) {
			obj = createdobj;
		}
		System.out.println(obj);
	}

	private Object createobj() {
		Object obj;
		obj = new Object();
		int b = 2;
		return obj;
	}
	
	public void doCreateValue5() {
		Object obj = _classCaches.get("a_key");
		Object createdobj = new Object();
		if (_classCaches.putIfAbsent("a_key", createdobj) == null) {
			obj = createdobj;
			System.out.println(obj);
		}
	}

	public void doCreateValue6() {
		Object obj = _classCaches.get("a_key");
		Object createdobj = new Object();
		if (_classCaches.putIfAbsent("a_key", createdobj) == null) {
			obj = createdobj;
		}
		System.out.println(obj);
	}
}