package object_out;

import java.util.concurrent.atomic.AtomicInteger;

public class TestSubtract {

	AtomicInteger f = new AtomicInteger();

	void subtract() {
			f.addAndGet(-12);
		}
	}
}