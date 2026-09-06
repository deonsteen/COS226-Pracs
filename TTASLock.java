import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class TTASLock {
    
private final AtomicBoolean locked = new AtomicBoolean(false);

private final AtomicLong testAndSetCount = new AtomicLong(0);


private boolean testAndSet()
{
        return locked.getAndSet(true);
}

public void lock()
    {
        while (true)
        {
            //spin on a plain, which is non-atmoic. read whi;e lock appears held. 
            // No atomic op i invoked, no coherence traffic 
            while (locked.get())
            {
                //busy-wait
            }

           testAndSetCount.incrementAndGet(); //The lock appears free. Attempt atomic acquire
            if (!testAndSet())
            {
                return;//Successfully acquired thelock
            }
            //Someone else beat us to it, return to the cheap spin loop
        }
    }   

    public void unlock()
    {
        locked.set(false);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    //Klein bietjie hulp vir Taak 3:
    /** For Task 3's experiment: number of atomic testAndSet() invocations. */
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public long getTestAndSetCount()
    {
        return testAndSetCount.get();
    }

}


/**
 * OPTIMISED implementation (Task 2) — Test-and-Test-and-Set (TTAS) lock.
 *
 * Difference from TASLock: before attempting the atomic testAndSet()
 * operation, this lock first does a plain (non-atomic) read of the
 * lock's state. It only attempts the atomic operation once the lock
 * *appears* free. This avoids repeatedly invoking testAndSet() (and
 * therefore repeatedly generating cache-coherence traffic) while the
 * lock is known to be held by another thread.
 */