package object_out;

import java.util.concurrent.atomic.AtomicInteger;

public class TestSynchronizedBlockOneSingleAccess {

	AtomicInteger f = new AtomicInteger();

	void syncSingleAccess() {
		f.set(12);
	}
}