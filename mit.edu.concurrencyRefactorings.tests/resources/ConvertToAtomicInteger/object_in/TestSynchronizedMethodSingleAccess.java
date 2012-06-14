package object_in;

public class TestSynchronizedMethodSingleAccess {

	int f;

	synchronized void syncSingleAccess() {
		f = 12;
	}
}