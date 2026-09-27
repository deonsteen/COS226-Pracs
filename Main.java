public class Main {

    public static void main(String[] args) throws InterruptedException {

        int operationsPerThread = 5000;

        for (int numberOfThreads = 2; numberOfThreads <= 16; numberOfThreads *= 2) {
            CoarseList CoarseList = new CoarseList();
            FineList FineList = new FineList();
            // Coarse-grained run
            Thread[] coarseThreads = new Thread[numberOfThreads];

            long coarseStartTime = System.nanoTime();

            for (int i = 0; i < numberOfThreads; i++) {
                final int threadID = i;

                coarseThreads[i] = new Thread(() -> {

                    for (int j = 0; j < operationsPerThread; j++) {
                        int value = (threadID * 1000) + (j % 1000);

                        if (j % 3 == 0) {
                            CoarseList.add(value);
                        } else if (j % 3 == 1) {
                            CoarseList.contains(value);
                        } else {
                            CoarseList.remove(value);
                        }
                    }
                });

                coarseThreads[i].start();
            }

            for (Thread thread : coarseThreads) {
                thread.join();
            }
            long coarseEndTime = System.nanoTime();

            double coarseTime = (coarseEndTime - coarseStartTime) / 1000000.0;

            // Fine-grained run
            Thread[] fineThreads = new Thread[numberOfThreads];

            long fineStartTime = System.nanoTime();

            for (int i = 0; i < numberOfThreads; i++) {
                final int threadID = i;

                fineThreads[i] = new Thread(() -> {

                    for (int j = 0; j < operationsPerThread; j++) {
                        int value = (threadID * 1000) + (j % 1000);

                        if (j % 3 == 0) {
                            FineList.add(value);
                        } else if (j % 3 == 1) {
                            FineList.contains(value);
                        } else {
                            FineList.remove(value);
                        }
                    }
                });

                fineThreads[i].start();
            }

            for (Thread thread : fineThreads) {
                thread.join();
            }
            long fineEndTime = System.nanoTime();

            double fineTime = (fineEndTime - fineStartTime) / 1000000.0;

            System.out.println(numberOfThreads + " threads | Coarse: " + coarseTime + " ms | Fine: " + fineTime + " ms");
        }
    }
}