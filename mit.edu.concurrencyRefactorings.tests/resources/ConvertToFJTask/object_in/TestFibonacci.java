package object_in;

public class TestFibonacci {
	
	public int fibonacci(int end) {
		if (end < 2) {
			return end;
		}
		else {
			return fibonacci(end - 1) + fibonacci(end - 2);
		}
	}
}