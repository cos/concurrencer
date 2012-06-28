package object_in;

public class TestReturnMultipleTasks {
	
	public int method(int num) {
		if (num <= 0) {
			return 0;
		} else {
			return method(num - 1) + method(num - 2) + method(num - 3);
		}
	}
}