package object_in;

public class TestFibonacciCombination {
	
	public int fibonacciCombination(int end) {
		if (end < 2) {
			return end;
		}
		else {
			int i = fibonacciCombination(end - 1);
			return i + fibonacciCombination(end - 2);
		}
	}
}