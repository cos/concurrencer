package invalid;

public class TestCannotRefactorFieldAccessedTwiceInSynchronizedBlock {

	int f;

	void twoFieldsInSyncBlock() {
		synchronized (this) {
			f = f + 12;
			f++;
		}
	}
}
