package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class TestMethodMultipleTasks {
	
	public int method(int num) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		MethodImpl aMethodImpl = new MethodImpl(num);
		pool.invoke(aMethodImpl);
		return aMethodImpl.result;
	}
	public class MethodImpl extends RecursiveAction {
		private int num;
		private int result;
		private MethodImpl(int num) {
			this.num = num;
		}
		protected void compute() {
			if ((num < 10)) {
				result = method(num);
				return;
			} else {
				MethodImpl task1 = new MethodImpl(num - 1);
				MethodImpl task2 = new MethodImpl(num - 2);
				MethodImpl task3 = new MethodImpl(num - 3);
				invokeAll(task1, task2, task3);
				result = sum(task1.result, task2.result, task3.result);
			}
		}
		public int method(int num) {
			if (num <= 0) {
				return 0;
			} else {
				return sum(method(num - 1), method(num - 2), method(num - 3));
			}
		}
	}
	public int sum(int a, int b, int c) {
		return a + b + c;
	}
}