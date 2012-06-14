package object_out;

import java.util.concurrent.atomic.AtomicInteger;

public class TestMultipleFields {

	AtomicInteger f = new AtomicInteger();
	AtomicInteger g = new AtomicInteger()

	void inc() {
		f.getAndIncrement();
		g.getAndIncrement();
	}
}