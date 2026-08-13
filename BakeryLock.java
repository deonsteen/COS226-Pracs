public class BakeryLock implements Lock 
{

    private final int n;
    private final VolatileBoolean[] flag;
    private final VolatileInt[] label;

    public BakeryLock(int n) 
    {

    }

    @Override
    public void lock(int threadId) 
    {

    }

    @Override
    public void unlock(int threadId) 
    {
        
    }
}