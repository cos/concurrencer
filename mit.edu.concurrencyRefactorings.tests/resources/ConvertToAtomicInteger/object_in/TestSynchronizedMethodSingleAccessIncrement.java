package object_in;

public class TestSynchronizedMethodSingleAccessIncrement {

	int f;

	synchronized void syncSingleAccessInc() {
		f++;
	}
}