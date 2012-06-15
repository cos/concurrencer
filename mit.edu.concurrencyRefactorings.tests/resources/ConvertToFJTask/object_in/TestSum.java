package object_in;

public class TestSum {
	
	public int recursionSum(int end) {
		if (end <= 0) {
			return 0;
		} else {
			return sum(recursionSum(end - 1), recursionSum(end - 2));
		}
	}
	public int sum(int a, int b) {
		return a + b;
	}
}