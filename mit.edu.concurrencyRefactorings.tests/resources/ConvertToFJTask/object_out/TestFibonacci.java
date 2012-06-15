package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestFibonacci {
	
	public int fibonacci(int end) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		FibonacciImpl aFibonacciImpl = new FibonacciImpl(end);
		pool.invoke(aFibonacciImpl);
		return aFibonacciImpl.result;
	}

	public class FibonacciImpl extends RecursiveAction {
		private int end;
		private int result;
		private FibonacciImpl(int end) {
			this.end = end;
		}
		protected void compute() {
			if ((end < 10)) {
				result = fibonacci(end);
				return;
			} else {
				FibonacciImpl task1 = new FibonacciImpl(end - 1);
				FibonacciImpl task2 = new FibonacciImpl(end - 2);
				invokeAll(task1, task2);
				result = task1.result + task2.result;
			}
		}
		public int fibonacci(int end) {
			if (end < 2) {
				return end;
			} else {
				return fibonacci(end - 1) + fibonacci(end - 2);
			}
		}
	}
}