package object_in;

public class TestSynchronizedBlockOneSingleAccess {

	int f;

	void syncSingleAccess() {
		synchronized (this) {
			f = 12;
		}
	}
}