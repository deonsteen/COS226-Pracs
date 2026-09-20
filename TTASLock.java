import java.util.concurrent.atomic.AtomicBoolean;

public class TTASLock implements Lock{


    //Starts ar false (unlocked), so that first thread to call lock() can succeed
    private AtomicBoolean state = new AtomicBoolean(false);

    @Override
    public void lock()
    {
        while (true)
        {
            //Spin while lock looks held. .get() cheaper than compare and set method 
            while (state.get())
            {
               //busy-wait
            }
        
            //attempt the real atomic acquisition.
            // Only succeeds if state is STILL false at this exact instant;
            // if another thread grabbed it between our read and this CAS,
            // this returns false and we loop back to spin-reading again.
        if (state.compareAndSet(false, true))
        {
            return;
        }
        //Compare And Set failed , a thread beat us, fall through, loop back, retry
    }
}



@Override 
public void unlock() 
{
    state.set(false);
}
}