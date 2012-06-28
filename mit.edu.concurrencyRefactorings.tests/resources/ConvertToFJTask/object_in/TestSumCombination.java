package object_in;

public class TestSumCombination {
	
	public int recursionSumCombination(int end) {
		if (end <= 0) {
			return 0;
		} else {
			int i = recursionSumCombination(end - 1);
			return sumCombination(i, recursionSumCombination(end - 2));
		}
	}
	public int sumCombination(int a, int b) {
		return a + b;
	}
}