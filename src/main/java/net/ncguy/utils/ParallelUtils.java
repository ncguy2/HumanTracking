package net.ncguy.utils;

import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

public class ParallelUtils {

    static ForkJoinPool pool;
    public static ForkJoinPool Pool() {
        if (pool == null)
            pool = new ForkJoinPool();
        return pool;
    }

    public static void Parallel(int start, int max, int step, Consumer<Integer> task) {
        for(int i = start; i < max; i+=step) {
            int finalI = i;
            Pool().execute(() -> task.accept(finalI));
        }
    }

}
