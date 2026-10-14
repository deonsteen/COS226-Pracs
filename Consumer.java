import java.util.concurrent.atomic.AtomicInteger;

/**
 * Consumer thread: removes jobs with deq() and records them as processed.
 *
 * Termination: before each deq() the consumer "claims" one job from the shared
 * 'remaining' counter. A successful claim guarantees a job will eventually be
 * produced for it, so:
 *   - with the bounded queue, deq() can never block forever;
 *   - the total number of successful deq() calls equals the total jobs.
 *
 * deq() returning null (lock-free queue, queue momentarily empty) is NOT a
 * processed job: the consumer retries the same claim.
 */
public class Consumer extends Thread {

    private final ConcurrentQueue<Job> queue;
    private final AtomicInteger remaining;
    private final ProcessedLog log;

    private int consumed = 0;
    private long emptyPolls = 0;

    public Consumer(ConcurrentQueue<Job> queue, AtomicInteger remaining, ProcessedLog log) {
        this.queue = queue;
        this.remaining = remaining;
        this.log = log;
    }

    @Override
    public void run() {
        while (remaining.getAndDecrement() > 0) {
            Job job = queue.deq();
            while (job == null) {          // empty lock-free queue: try again
                emptyPolls++;
                Thread.onSpinWait();
                job = queue.deq();
            }
            log.record(job);               // "processing" = record it
            consumed++;
        }
    }

    /** Only safe to read after join(). */
    public int getConsumed() {
        return consumed;
    }

    public long getEmptyPolls() {
        return emptyPolls;
    }
}