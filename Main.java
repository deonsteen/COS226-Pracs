public class Main {

    public static void main(String[] args) throws InterruptedException {
        int numberOfThreads = 4;
        int iterations = 200;

        Lock[] locks = { new TTASLock(), new CLHLock(), new MCSLock() };
        String[] names = { "TTAS", "CLH", "MCS" };

        for (int i = 0; i < locks.length; i++)
        {
            System.out.println("Running with " + names[i] + "Lock ");
            Auction auction = new Auction(AuctionUtils.generateItemName());
            Runner runner = new Runner(numberOfThreads, iterations, auction, locks[i]);
            runner.run();
            System.out.println();
        }
    }
}
