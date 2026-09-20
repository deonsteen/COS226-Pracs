public class MCSLockTest {

    public static void main(String[] args) throws InterruptedException
    {
        MCSLock lock = new MCSLock();

        int[] counter = {0};
        int numThreads = 10;
        int incrementsPerThread = 100000;

        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++)
        {
            threads[i] = new Thread(() ->
            {
                for (int j = 0; j < incrementsPerThread; j++)
                {
                    lock.lock();
                    counter[0] = counter[0] + 1;
                    lock.unlock();
                }
            });
        }

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        int expected = numThreads * incrementsPerThread;
        System.out.println("Expected: " + expected);
        System.out.println("Actual: " + counter[0]);
        System.out.println(counter[0] == expected ? "PASS - mutual exclusion held" : "FAIL - lost updates detected");
    }
}
