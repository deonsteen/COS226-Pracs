import java.util.function.Supplier;

// Task 3: contention experiment comparing TASLock vs TTASLock.
public class Main
{

    // Contention levels to test, per the spec (min 2..32 threads).
    private static final int[] THREAD_COUNTS = {2, 4, 8, 16, 32};
    // Spec requires each configuration run at least 5 times.
    private static final int RUNS_PER_CONFIG = 5;
    // Fixed per-thread workload so TAS and TTAS do identical work per config.
    private static final int INCREMENTS_PER_THREAD = 100_000;

    public static void main(String[] args) throws InterruptedException
    {
        // Step 1: prove both locks give mutual exclusion (required for demo).
        System.out.println("=== Mutual exclusion check ===");
        checkMutualExclusion("TASLock", TASLock::new);
        checkMutualExclusion("TTASLock", TTASLock::new);

        // Step 2: measure how each lock behaves as contention increases.
        System.out.println();
        System.out.println("=== Contention experiment (" + RUNS_PER_CONFIG + " runs each, "
                + INCREMENTS_PER_THREAD + " increments/thread) ===");
        System.out.printf("%-8s %14s %20s %14s %20s%n",
                "Threads", "TAS avg (ms)", "TAS avg TAS-count", "TTAS avg (ms)", "TTAS avg TAS-count");

        for (int threads : THREAD_COUNTS)
        {
            // Same thread count and workload for both locks -> fair comparison.
            double[] tas = runConfiguration(TASLock::new, threads);
            double[] ttas = runConfiguration(TTASLock::new, threads);

            System.out.printf("%-8d %14.3f %20.1f %14.3f %20.1f%n",
                    threads, tas[0], tas[1], ttas[0], ttas[1]);
        }
        // Expect: TAS-count grows much faster than TTAS-count as threads increase.
        // Theory: TAS retries the atomic op on every spin (cache-coherence traffic
        // on every attempt); TTAS spins on a plain read first and only attempts
        // the atomic op once the lock looks free, so it does far fewer atomic ops
        // under contention.
    }

    // Runs many threads incrementing a shared counter under one lock.
    // If the lock is correct, actual == expected every time (no lost updates).
    private static void checkMutualExclusion(String name, Supplier<Lock> factory) throws InterruptedException
    {
        int threads = 8;
        int incrementsPerThread = 50_000;
        Lock lock = factory.get();
        int[] counter = {0}; // array trick: lambdas need an effectively-final reference

        Thread[] pool = new Thread[threads];
        for (int i = 0; i < threads; i++)
        {
            pool[i] = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++)
                {
                    // Critical section: only one thread may be inside at a time.
                    lock.lock();
                    counter[0]++;
                    lock.unlock();
                }
            });
            pool[i].start();
        }
        for (Thread t : pool)
        {
            t.join(); // wait for all threads before checking the result
        }

        int expected = threads * incrementsPerThread;
        String verdict = (expected == counter[0]) ? "OK" : "MUTUAL EXCLUSION VIOLATED";
        System.out.println(name + " -> expected: " + expected + ", actual: " + counter[0] + "  [" + verdict + "]");
    }

    // Times numThreads competing for one lock, RUNS_PER_CONFIG times, and
    // returns the averaged {execution time (ms), testAndSet() invocation count}.
    private static double[] runConfiguration(Supplier<Lock> factory, int numThreads) throws InterruptedException
    {
        long totalTimeNanos = 0;
        long totalTasCount = 0;

        for (int run = 0; run < RUNS_PER_CONFIG; run++)
        {
            // Fresh lock + counter each run so runs don't affect each other.
            Lock lock = factory.get();
            int[] counter = {0};
            Thread[] threads = new Thread[numThreads];

            long start = System.nanoTime();
            for (int i = 0; i < numThreads; i++)
            {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < INCREMENTS_PER_THREAD; j++)
                    {
                        lock.lock();
                        counter[0]++;
                        lock.unlock();
                    }
                });
                threads[i].start();
            }
            for (Thread t : threads)
            {
                t.join(); // wait for every thread to finish before stopping the clock
            }
            long end = System.nanoTime();

            totalTimeNanos += (end - start);
            // More contention (more threads) -> more retries -> higher count.
            totalTasCount += lock.getTestAndSetCount();
        }

        // Average across the RUNS_PER_CONFIG runs to smooth out noise
        // (OS scheduling, JVM warm-up, background load, etc).
        double avgTimeMs = (totalTimeNanos / (double) RUNS_PER_CONFIG) / 1_000_000.0;
        double avgTasCount = totalTasCount / (double) RUNS_PER_CONFIG;
        return new double[]{avgTimeMs, avgTasCount};
    }
}
