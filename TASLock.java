import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ORIGINAL implementation (Task 1) — basic Test-and-Set lock.
 * Spins by repeatedly invoking the atomic testAndSet() operation,
 * even while the lock is known to be held.
 */
public class TASLock implements Lock
{

    private final AtomicBoolean locked = new AtomicBoolean(false);

    private final AtomicLong testAndSetCount = new AtomicLong(0);

    /* Do not modify this method */
    private boolean testAndSet()
    {
        return locked.getAndSet(true);
    }

    public void lock()
    {
        testAndSetCount.incrementAndGet();
        while (testAndSet())
        {
            testAndSetCount.incrementAndGet();
        }
    }

    public void unlock()
    {
        locked.set(false);
    }

    /** For Task 3's experiment: number of atomic testAndSet() invocations. */
    public long getTestAndSetCount()
    {
        return testAndSetCount.get();
    }

}