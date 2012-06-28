package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestFibonacciCombination {
	
	public int fibonacciCombination(int end) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		FibonacciCombinationImpl aFibonacciCombinationImpl = new FibonacciCombinationImpl(end);
		pool.invoke(aFibonacciCombinationImpl);
		return aFibonacciCombinationImpl.result;
	}

	public class FibonacciCombinationImpl extends RecursiveAction {
		private int end;
		private int result;
		private FibonacciCombinationImpl(int end) {
			this.end = end;
		}
		protected void compute() {
			if ((end < 10)) {
				result = fibonacciCombination(end);
				return;
			} else {
				FibonacciCombinationImpl task1 = new FibonacciCombinationImpl(end - 1);
				FibonacciCombinationImpl task2 = new FibonacciCombinationImpl(end - 2);
				invokeAll(task1, task2);
				int i = task1.result;
				result = i + task2.result;
			}
		}
		public int fibonacciCombination(int end) {
			if (end < 2) {
				return end;
			} else {
				int i = fibonacciCombination(end - 1);
				return i + fibonacciCombination(end - 2);
			}
		}
	}
}