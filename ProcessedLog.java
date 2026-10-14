import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Thread-safe record of which jobs have been processed.
 * Job ids are 0..totalJobs-1, so each id owns one slot in the array.
 * A slot should end up with exactly 1 (0 = lost job, >1 = duplicated job).
 */
public class ProcessedLog {

    private final AtomicIntegerArray seen;
    private final AtomicInteger processed = new AtomicInteger(0);

    public ProcessedLog(int totalJobs) {
        this.seen = new AtomicIntegerArray(totalJobs);
    }

    public void record(Job job) {
        seen.incrementAndGet((int) job.getId());
        processed.incrementAndGet();
    }

    public int getProcessedCount() {
        return processed.get();
    }

    /** Number of ids that were never processed. */
    public int countMissing() {
        int missing = 0;
        for (int i = 0; i < seen.length(); i++) {
            if (seen.get(i) == 0) missing++;
        }
        return missing;
    }

    /** Number of ids that were processed more than once. */
    public int countDuplicates() {
        int dups = 0;
        for (int i = 0; i < seen.length(); i++) {
            if (seen.get(i) > 1) dups++;
        }
        return dups;
    }
}