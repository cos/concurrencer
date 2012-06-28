package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestSum {
	
	public int recursionSum(int end) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		RecursionSumImpl aRecursionSumImpl = new RecursionSumImpl(end);
		pool.invoke(aRecursionSumImpl);
		return aRecursionSumImpl.result;
	}
	public class RecursionSumImpl extends RecursiveAction {
		private int end;
		private int result;
		private RecursionSumImpl(int end) {
			this.end = end;
		}
		protected void compute() {
			if ((end < 5)) {
				result = recursionSum(end);
				return;
			} else {
				RecursionSumImpl task1 = new RecursionSumImpl(end - 1);
				RecursionSumImpl task2 = new RecursionSumImpl(end - 2);
				invokeAll(task1, task2);
				result = sum(task1.result, task2.result);
			}
		}
		public int recursionSum(int end) {
			if (end <= 0) {
				return 0;
			} else {
				return sum(recursionSum(end - 1), recursionSum(end - 2));
			}
		}
	}
	public int sum(int a, int b) {
		return a + b;
	}
}