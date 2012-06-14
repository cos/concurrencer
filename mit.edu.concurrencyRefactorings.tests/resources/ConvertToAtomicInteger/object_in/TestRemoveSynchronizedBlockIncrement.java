package object_in;

public class TestRemoveSynchronizedBlockIncrement {

	int f;

	void syncIncrement() {
		synchronized (this) {
			f++;
		}
	}
}