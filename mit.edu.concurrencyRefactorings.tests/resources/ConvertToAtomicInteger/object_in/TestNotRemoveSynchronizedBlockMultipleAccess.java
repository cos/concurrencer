package object_in;

public class TestNotRemoveSynchronizedBlockMultipleAccess {

	int f;

	void syncMultipleAccess() {
		synchronized(this) {
			f++;
			f++;
		}
	}
}