package object_in;

public class TestMethodMultipleTasks {
	
	public int method(int num) {
		if (num <= 0) {
			return 0;
		} else {
			return sum(method(num - 1), method(num - 2), method(num - 3));
		}
	}
	public int sum(int a, int b, int c) {
		return a + b + c;
	}
}