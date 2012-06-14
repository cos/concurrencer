package object_out;

import java.util.concurrent.atomic.AtomicInteger;

public class TestRemoveSynchronizedBlockIncrement {

	AtomicInteger f = new AtomicInteger();

	void syncIncrement() {
		f.getAndIncrement();
	}
}