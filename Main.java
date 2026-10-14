import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Usage:
 *   java Main                                  -> full experiment (all 6 combinations)
 *   java Main bounded|lockfree P C [jobs]      -> single demo run with verification
 */
public class Main {

    static final int BOUNDED_CAPACITY = 100;    // same capacity for every run
    static final int TOTAL_JOBS = 120_000;      // divisible by 2, 4 and 6
    static final int REPEATS = 5;               // timed runs averaged per configuration
    static final int[][] CONFIGS = { {4, 4}, {6, 2}, {2, 6} };

    static class Result {
        String queue; int producers; int consumers; int totalJobs;
        double seconds; long emptyPolls; boolean correct;
        double throughput() { return totalJobs / seconds; }
    }

    static ConcurrentQueue<Job> makeQueue(String type) {
        if (type.equalsIgnoreCase("bounded")) return new BoundedQueue<>(BOUNDED_CAPACITY);
        if (type.equalsIgnoreCase("lockfree")) return new LockFreeQueue<>();
        throw new IllegalArgumentException("Queue must be 'bounded' or 'lockfree'");
    }

    /** One complete producer-consumer run. */
    static Result run(String type, int numProducers, int numConsumers, int totalJobs)
            throws InterruptedException {

        ConcurrentQueue<Job> queue = makeQueue(type);
        ProcessedLog log = new ProcessedLog(totalJobs);
        AtomicInteger remaining = new AtomicInteger(totalJobs);

        Producer[] producers = new Producer[numProducers];
        Consumer[] consumers = new Consumer[numConsumers];

        // Split the workload; any remainder goes to the first producers.
        int base = totalJobs / numProducers, extra = totalJobs % numProducers;
        long nextId = 0;
        for (int i = 0; i < numProducers; i++) {
            int count = base + (i < extra ? 1 : 0);
            producers[i] = new Producer(queue, nextId, count);
            nextId += count;
        }
        for (int i = 0; i < numConsumers; i++) {
            consumers[i] = new Consumer(queue, remaining, log);
        }

        long start = System.nanoTime();
        for (Producer p : producers) p.start();
        for (Consumer c : consumers) c.start();
        for (Producer p : producers) p.join();
        for (Consumer c : consumers) c.join();
        long end = System.nanoTime();

        int produced = 0, consumed = 0; long polls = 0;
        for (Producer p : producers) produced += p.getProduced();
        for (Consumer c : consumers) { consumed += c.getConsumed(); polls += c.getEmptyPolls(); }

        Result r = new Result();
        r.queue = type; r.producers = numProducers; r.consumers = numConsumers;
        r.totalJobs = totalJobs; r.seconds = (end - start) / 1e9; r.emptyPolls = polls;
        r.correct = produced == totalJobs && consumed == totalJobs
                && log.getProcessedCount() == totalJobs
                && log.countMissing() == 0 && log.countDuplicates() == 0;
        return r;
    }

    static void demo(String type, int p, int c, int jobs) throws InterruptedException {
        ConcurrentQueue<Job> q = makeQueue(type);   // validates the name early
        System.out.println("Queue: " + q.getClass().getSimpleName()
                + (type.equalsIgnoreCase("bounded") ? " (capacity " + BOUNDED_CAPACITY + ")" : ""));
        System.out.println("Producers: " + p + ", Consumers: " + c);
        Result r = run(type, p, c, jobs);
        System.out.println("Jobs produced:  " + jobs);
        System.out.println("Jobs processed: " + (r.correct ? jobs : "MISMATCH"));
        System.out.printf(Locale.US, "Time: %.4f s, Throughput: %.0f jobs/s%n", r.seconds, r.throughput());
        System.out.println("Empty deq() polls (lock-free only): " + r.emptyPolls);
        System.out.println(r.correct
                ? "VERIFIED: every job processed exactly once (no losses, no duplicates)."
                : "FAILED: jobs were lost or duplicated!");
    }

    static void experiment() throws InterruptedException {
        String[] types = {"bounded", "lockfree"};

        // Warm-up so the JIT has compiled everything before timing
        for (String t : types) for (int[] cfg : CONFIGS) run(t, cfg[0], cfg[1], TOTAL_JOBS / 4);

        System.out.println("Total jobs per run: " + TOTAL_JOBS
                + " | bounded capacity: " + BOUNDED_CAPACITY
                + " | each row = average of " + REPEATS + " runs\n");
        System.out.printf("%-10s %9s %9s %12s %16s %14s %9s%n",
                "Queue", "Producers", "Consumers", "Time (s)", "Throughput (j/s)", "Empty polls", "Correct");

        StringBuilder csv = new StringBuilder("queue,producers,consumers,jobs,time_s,throughput,empty_polls,correct\n");

        for (int[] cfg : CONFIGS) {
            for (String t : types) {
                double secs = 0; long polls = 0; boolean ok = true;
                for (int i = 0; i < REPEATS; i++) {
                    Result r = run(t, cfg[0], cfg[1], TOTAL_JOBS);
                    secs += r.seconds; polls += r.emptyPolls; ok &= r.correct;
                }
                secs /= REPEATS; polls /= REPEATS;
                double thr = TOTAL_JOBS / secs;
                System.out.printf(Locale.US, "%-10s %9d %9d %12.4f %16.0f %14d %9s%n",
                        t, cfg[0], cfg[1], secs, thr, polls, ok ? "yes" : "NO");
                csv.append(String.format(Locale.US, "%s,%d,%d,%d,%.4f,%.0f,%d,%s%n",
                        t, cfg[0], cfg[1], TOTAL_JOBS, secs, thr, polls, ok));
            }
        }
        System.out.println("\nCSV:\n" + csv);
    }

    public static void main(String[] args) throws InterruptedException {
        if (args.length == 0) {
            experiment();
        } else if (args.length >= 3) {
            int jobs = args.length >= 4 ? Integer.parseInt(args[3]) : TOTAL_JOBS;
            demo(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), jobs);
        } else {
            System.out.println("Usage: java Main [bounded|lockfree P C [jobs]]");
        }
    }
}