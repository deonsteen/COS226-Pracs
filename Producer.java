/**
 * Producer thread: creates jobs with ids in [firstId, firstId + jobsToProduce)
 * and places each one in the shared queue using enq().
 * Id ranges are disjoint between producers, so every job id is unique
 * without needing a shared (contended) counter.
 */
public class Producer extends Thread {

    private final ConcurrentQueue<Job> queue;
    private final long firstId;
    private final int jobsToProduce;
    private int produced = 0;

    public Producer(ConcurrentQueue<Job> queue, long firstId, int jobsToProduce) {
        this.queue = queue;
        this.firstId = firstId;
        this.jobsToProduce = jobsToProduce;
    }

    @Override
    public void run() {
        for (int i = 0; i < jobsToProduce; i++) {
            queue.enq(new Job(firstId + i));
            produced++;
        }
    }

    /** Only safe to read after join(). */
    public int getProduced() {
        return produced;
    }
}