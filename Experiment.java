import java.io.FileWriter;
import java.io.IOException;

/*
 * Task 3 harness: runs every (lock, thread count) configuration required by
 * the spec, repeated multiple times, and writes one row of RAW data per run
 * to results.csv. No averaging happens here - that is the analysis step.
 */
public class Experiment
{
    private static final int[] THREAD_COUNTS = {2, 4, 8, 16};
    private static final int ITERATIONS_PER_THREAD = 50_000;
    private static final int REPEATS = 3;
    private static final String[] LOCK_NAMES = {"TTAS", "CLH", "MCS"};
    private static final String OUTPUT_FILE = "results.csv";

    public static void main(String[] args) throws Exception
    {
        try (FileWriter csv = new FileWriter(OUTPUT_FILE))
        {
            csv.write("lock,threads,iterationsPerThread,run,executionTimeNanos,totalBids,finalHighestBid,winCounts\n");

            for (int threads : THREAD_COUNTS)
            {
                for (String lockName : LOCK_NAMES)
                {
                    for (int run = 1; run <= REPEATS; run++)
                    {
                        runOnce(csv, lockName, threads, run);
                    }
                }
            }
        }

        System.out.println("Done. Raw results written to " + OUTPUT_FILE);
    }

    private static void runOnce(FileWriter csv, String lockName, int threads, int run) throws InterruptedException, IOException
    {
        Lock lock = createLock(lockName);
        Auction auction = new Auction(AuctionUtils.generateItemName());
        Runner runner = new Runner(threads, ITERATIONS_PER_THREAD, auction, lock);

        runner.run();

        int totalBids = runner.getTotalBids();
        int expectedBids = threads * ITERATIONS_PER_THREAD;
        if (totalBids != expectedBids)
        {
            System.err.println("WARNING: " + lockName + " threads=" + threads + " run=" + run
                + " lost updates - expected " + expectedBids + " but got " + totalBids);
        }

        StringBuilder winCounts = new StringBuilder();
        Bidder[] bidders = runner.getBidders();
        for (int i = 0; i < bidders.length; i++)
        {
            if (i > 0) winCounts.append(';');
            winCounts.append(bidders[i].getWinCount());
        }

        System.out.printf("lock=%-5s threads=%-3d run=%d execTimeMs=%.3f totalBids=%d finalBid=%.2f%n",
            lockName, threads, run, runner.getExecutionTimeNanos() / 1_000_000.0, totalBids, auction.getHighestBid());

        csv.write(String.join(",",
            lockName,
            String.valueOf(threads),
            String.valueOf(ITERATIONS_PER_THREAD),
            String.valueOf(run),
            String.valueOf(runner.getExecutionTimeNanos()),
            String.valueOf(totalBids),
            String.valueOf(auction.getHighestBid()),
            winCounts.toString()
        ));
        csv.write("\n");
    }

    private static Lock createLock(String name)
    {
        switch (name)
        {
            case "CLH": return new CLHLock();
            case "MCS": return new MCSLock();
            case "TTAS": return new TTASLock();
            default: throw new IllegalArgumentException("Unknown lock: " + name);
        }
    }
}
