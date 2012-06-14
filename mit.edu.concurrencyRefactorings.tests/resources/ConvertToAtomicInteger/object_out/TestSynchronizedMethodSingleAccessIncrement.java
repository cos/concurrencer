package object_out;

import java.util.concurrent.atomic.AtomicInteger;

public class TestSynchronizedMethodSingleAccessIncrement {

	AtomicInteger f = new AtomicInteger();

	void syncSingleAccessInc() {
		f.getAndIncrement();
	}
}