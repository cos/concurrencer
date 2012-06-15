package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import forkJoin.mergeSort.ArrayUtil;

public class SeqQuickSort {
	//private static long comparisons = 0;
	//private static long exchanges   = 0;

	/***********************************************************************
	 *  Quicksort code from Sedgewick 7.1, 7.2.
	 ***********************************************************************/

	// quicksort a[left] to a[right]
	public void quicksort(double[] a, int left, int right) {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		QuicksortImpl aQuicksortImpl = new QuicksortImpl(a, left, right);
		pool.invoke(aQuicksortImpl);
	}

	public class QuicksortImpl extends RecursiveAction {
		private double[] a;
		private int left;
		private int right;
		private QuicksortImpl(double[] a, int left, int right) {
			this.a = a;
			this.left = left;
			this.right = right;
		}
		protected void compute() {
			if ((right - left < 10)) {
				quicksort(a, left, right);
				return;
			}
			int i = partition(a, left, right);
			QuicksortImpl task1 = new QuicksortImpl(a, left, i - 1);
			QuicksortImpl task2 = new QuicksortImpl(a, i + 1, right);
			invokeAll(task1, task2);
		}
		/**
		 * Quicksort code from Sedgewick 7.1, 7.2.
		 */
		public void quicksort(double[] a, int left, int right) {
			if (right <= left)
				return;
			int i = partition(a, left, right);
			quicksort(a, left, i - 1);
			quicksort(a, i + 1, right);
		}
	}

	// partition a[left] to a[right], assumes left < right
	private int partition(double[] a, int left, int right) {
		int i = left - 1;
		int j = right;
		while (true) {
			while (less(a[++i], a[right]))      // find item on left to swap
				;                               // a[right] acts as sentinel
			while (less(a[right], a[--j]))      // find item on right to swap
				if (j == left) break;           // don't go out-of-bounds
			if (i >= j) break;                  // check if pointers cross
			swap(a, i, j);                      // swap two elements into place
		}
		swap(a, i, right);                      // swap with partition element
		return i;
	}

	// is x < y ?
	private boolean less(double x, double y) {
		//  comparisons++;
		return (x < y);
	}

	// exchange a[i] and a[j]
	private void swap(double[] a, int i, int j) {
		//exchanges++;
		double swap = a[i];
		a[i] = a[j];
		a[j] = swap;
	}

	// shuffle the array a[]
	private void shuffle(double[] a) {
		int N = a.length;
		for (int i = 0; i < N; i++) {
			int r = i + (int) (Math.random() * (N-i));   // between i and N-1
			swap(a, i, r);
		}
	}

	public void quicksort(double[] a) {
		shuffle(a);                        // to guard against worst-case
		quicksort(a, 0, a.length - 1);
	}

	// test client
	public static void main(String[] args) {
		int N = 100;

		// generate N random real numbers between 0 and 1
		long start = System.currentTimeMillis();
		double[] a = new double[N];
		for (int i = 0; i < N; i++)
			a[i] = Math.random();
		long stop = System.currentTimeMillis();
		double elapsed = (stop - start) / 1000.0;
		System.out.println("Generating input:  " + elapsed + " seconds");

		SeqQuickSort seqQuickSort = new SeqQuickSort();
		seqQuickSort.shuffle(a);

		ArrayUtil.printArray(a, 5);
		// sort them
		start = System.currentTimeMillis();
		seqQuickSort.quicksort(a, 0, a.length - 1);
		stop = System.currentTimeMillis();
		//      ArrayUtil.printArray(a, 4);
		elapsed = (stop - start) / 1000.0;
		System.out.println("Quicksort:   " + elapsed + " seconds");
		ArrayUtil.printArray(a, 5);
	}
}