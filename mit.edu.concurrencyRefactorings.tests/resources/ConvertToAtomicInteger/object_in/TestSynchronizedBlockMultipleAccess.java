package object_in;

public class TestSynchronizedBlockMultipleAccess {

	int f;

	void syncMultipleAccess() {
		synchronized (this) {
			f = 12;
			f++;
		}
	}
}