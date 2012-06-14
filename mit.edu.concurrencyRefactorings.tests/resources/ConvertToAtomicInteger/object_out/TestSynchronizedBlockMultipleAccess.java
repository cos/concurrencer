package object_out;

import java.util.concurrent.atomic.AtomicInteger;

public class TestSynchronizedBlockMultipleAccess {

	AtomicInteger f = new AtomicInteger();

	void syncMultipleAccess() {
		synchronized (this) {
			f.set(12);
			f.getAndIncrement();
		}
	}
}