package object_out;

import java.util.concurrent.atomic.AtomicInteger;

public class TestSynchronizedMethodMultipleAccess {

	AtomicInteger f = new AtomicInteger();

	synchronized void syncMultipleAccess() {
		f.set(12);
		f.getAndIncrement();
	}
}