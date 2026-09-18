public class TTASLockTest {
    
    public static void main(String[] args) throws InterruptedException
    {
        TTASLock lock =new TTASLock();

        //Shared, mutable counter.
        //If lock correct, final number =numthreads 
        int[]counter= {0};
        int numThreads = 10;
        int incrementsPerThread = 100000;

        Thread[] threads = new Thread[numThreads];

        for (int i=0; i < numThreads; i++)
        {
            threads[i] = new Thread(() ->
        {
            for (int j = 0 ; j < incrementsPerThread; j++)
            {
                lock.lock();
                counter[0] = counter[0] + 1;//Critical section
                lock.unlock();
            }
        });
        }


        for (Thread t: threads) t.start();
        for (Thread t: threads) t.join();

        System.out.println("Expected: " + (numThreads * incrementsPerThread));
        System.out.println( "Actual: " + counter[0]);
    }
}
