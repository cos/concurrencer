package object_out;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class MaxSumTest {

    static private int seqStart = 0;
    static private int seqEnd = -1;

    /**
     * Cubic maximum contiguous subsequence sum algorithm.
     * seqStart and seqEnd represent the actual best sequence.
     */
    public static int maxSubSum1( int [ ] a )
    {
        int maxSum = 0;

        for( int i = 0; i < a.length; i++ )
            for( int j = i; j < a.length; j++ )
            {
                int thisSum = 0;

                for( int k = i; k <= j; k++ )
                    thisSum += a[ k ];

                if( thisSum > maxSum )
                {
                    maxSum   = thisSum;
                    seqStart = i;
                    seqEnd   = j;
                }
            }

        return maxSum;
    }

    /**
     * Recursive maximum contiguous subsequence sum algorithm.
     * Finds maximum sum in subarray spanning a[left..right].
     * Does not attempt to maintain actual best sequence.
     */
    private static int maxSumRec( int [ ] a, int left, int right )
    {
		int processorCount = Runtime.getRuntime().availableProcessors();
		ForkJoinPool pool = new ForkJoinPool(processorCount);
		MaxSumRecImpl aMaxSumRecImpl = new MaxSumRecImpl(a, left, right);
		pool.invoke(aMaxSumRecImpl);
		return aMaxSumRecImpl.result;
	}

	private static class MaxSumRecImpl extends RecursiveAction {
		private int[] a;
		private int left;
		private int right;
		private int result;
		private MaxSumRecImpl(int[] a, int left, int right) {
			this.a = a;
			this.left = left;
			this.right = right;
		}
		protected void compute() {
			int maxLeftBorderSum = 0, maxRightBorderSum = 0;
			int leftBorderSum = 0, rightBorderSum = 0;
			int center = (left + right) / 2;
			if ((right - left < 4)) {
				result = maxSumRec(a, left, right);
				return;
			}
			MaxSumRecImpl task1 = new MaxSumRecImpl(a, left, center);
			MaxSumRecImpl task2 = new MaxSumRecImpl(a, center + 1, right);
			invokeAll(task1, task2);
			int maxLeftSum = task1.result;
			int maxRightSum = task2.result;
			for (int i = center; i >= left; i--) {
				leftBorderSum += a[i];
				if (leftBorderSum > maxLeftBorderSum)
					maxLeftBorderSum = leftBorderSum;
			}
			for (int i = center + 1; i <= right; i++) {
				rightBorderSum += a[i];
				if (rightBorderSum > maxRightBorderSum)
					maxRightBorderSum = rightBorderSum;
			}
			result = max3(maxLeftSum, maxRightSum, maxLeftBorderSum + maxRightBorderSum);
		}
		/**
		 * Recursive maximum contiguous subsequence sum algorithm. Finds maximum sum in subarray spanning a[left..right]. Does not attempt to maintain actual best sequence.
		 */
		private static int maxSumRec(int[] a, int left, int right) {
			int maxLeftBorderSum = 0, maxRightBorderSum = 0;
			int leftBorderSum = 0, rightBorderSum = 0;
			int center = (left + right) / 2;
			if (left == right)
				return a[left] > 0 ? a[left] : 0;
			int maxLeftSum = maxSumRec(a, left, center);
			int maxRightSum = maxSumRec(a, center + 1, right);
			for (int i = center; i >= left; i--) {
				leftBorderSum += a[i];
				if (leftBorderSum > maxLeftBorderSum)
					maxLeftBorderSum = leftBorderSum;
			}
			for (int i = center + 1; i <= right; i++) {
				rightBorderSum += a[i];
				if (rightBorderSum > maxRightBorderSum)
					maxRightBorderSum = rightBorderSum;
			}
			return max3(maxLeftSum, maxRightSum, maxLeftBorderSum + maxRightBorderSum);
		}
	}

    /**
     * Return maximum of three integers.
     */
    private static int max3( int a, int b, int c )
    {
        return a > b ? a > c ? a : c : b > c ? b : c;
    }

    /**
     * Driver for divide-and-conquer maximum contiguous
     * subsequence sum algorithm.
     */
    public static int maxSubSum4( int [ ] a )
    {
        return a.length > 0 ? maxSumRec( a, 0, a.length - 1 ) : 0;
    }

    /**
     * Simple test program.
     */
    public static void main( String [ ] args )
    {
        int a[ ] = { 4, -3, 5, -2, -1, 2, 6, -2 };
        int maxSum;

        maxSum = maxSubSum1( a );
        System.out.println( "Max sum is " + maxSum + "; it goes"
                       + " from " + seqStart + " to " + seqEnd );
        
        maxSum = maxSubSum4( a );
        System.out.println( "Max sum is " + maxSum );
    }
}