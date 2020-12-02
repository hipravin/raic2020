package hipravin.alg;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BruteForceUtil {

    public static Stream<int[]> allCombinatonsOf(int n, int k) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new CombinationIterator(n, k), Spliterator.ORDERED), false);
    }

    static class CombinationIterator implements Iterator<int[]> {

        final int n;
        final int k;

        int[] current;
        int[] last;
        boolean init = true;

        public CombinationIterator(int n, int k) {
            this.n = n;
            this.k = k;

            current = new int[k];
            last = new int[k];
            for (int i = 0; i < k; i++) {
                current[i] = i;
                last[i] = i + (n - k);
            }
        }

        @Override
        public boolean hasNext() {
            return init || !Arrays.equals(current, last);
        }

        @Override
        public int[] next() {
            if(!init) {
                nextCombination();
            } else {
                init = false;
            }
            return current;
        }

        void nextCombination() {
            for (int i = current.length - 1; i >= 0; i--) {
                if (current[i] < n - k + i ) {
                    current[i]++;
                    for (int j = i + 1; j < k; j++) {

                        current[j] = current[j - 1] + 1;

                    }
                    return;
                }
            }
        }
    }

//    bool next_combination (vector<int> & a, int n) {
//        int k = (int)a.size();
//        for (int i=k-1; i>=0; --i)
//            if (a[i] < n-k+i+1) {
//                ++a[i];
//                for (int j=i+1; j<k; ++j)
//                    a[j] = a[j-1]+1;
//                return true;
//            }
//        return false;
//    }
}
