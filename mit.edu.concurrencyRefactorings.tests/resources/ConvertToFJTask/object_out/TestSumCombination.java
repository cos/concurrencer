package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestSumCombination {
	
	public int recursionSumCombination(int end) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		RecursionSumCombinationImpl aRecursionSumCombinationImpl = new RecursionSumCombinationImpl(end);
		pool.invoke(aRecursionSumCombinationImpl);
		return aRecursionSumCombinationImpl.result;
	}
	public class RecursionSumCombinationImpl extends RecursiveAction {
		private int end;
		private int result;
		private RecursionSumCombinationImpl(int end) {
			this.end = end;
		}
		protected void compute() {
			if ((end < 5)) {
				result = recursionSumCombination(end);
				return;
			} else {
				RecursionSumCombinationImpl task1 = new RecursionSumCombinationImpl(end - 1);
				RecursionSumCombinationImpl task2 = new RecursionSumCombinationImpl(end - 2);
				invokeAll(task1, task2);
				int i = task1.result;
				result = sumCombination(i, task2.result);
			}
		}
		public int recursionSumCombination(int end) {
			if (end <= 0) {
				return 0;
			} else {
				int i = recursionSumCombination(end - 1);
				return sumCombination(i, recursionSumCombination(end - 2));
			}
		}
	}
	public int sumCombination(int a, int b) {
		return a + b;
	}
}