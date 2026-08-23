COS 226 - Concurrent Systems
Practical 2: N-Thread Mutual Exclusion
================================================================

GROUP MEMBERS (Student Numbers)
--------------------------------
u________ - <Full Name>
u________ - <Full Name>
u________ - <Full Name>

(Replace the placeholders above with your actual student numbers and names
before submission.)


1. OVERVIEW
-----------
This submission implements two mutual exclusion algorithms that generalise
locking to an arbitrary number of concurrent threads:

  1. Filter Lock
  2. Bakery Lock

Both locks are used by the provided Main.java program to coordinate four
threads that each increment a shared Counter object. The purpose of the
practical is to demonstrate that, despite concurrent access, the counter
reaches the expected final value with no lost updates or race conditions.


2. FILES INCLUDED
------------------
  FilterLock.java     - Our implementation of the Filter Lock algorithm.
  BakeryLock.java      - Our implementation of Lamport's Bakery Lock algorithm.
  README.txt           - This file.

Supporting files provided as part of the practical (unmodified):
  Lock.java            - Interface defining lock(threadId) and unlock(threadId).
  Counter.java          - Shared counter incremented by competing threads.
  VolatileInt.java      - Wrapper providing volatile int access.
  VolatileBoolean.java  - Wrapper providing volatile boolean access.
  Main.java             - Driver program that creates threads and runs both locks.
  TaskGenerator.java    - Provides flavour text for console output during the demo.


3. HOW TO COMPILE AND RUN
--------------------------
From the project directory, compile all source files:

    javac *.java

Then run the Main class:

    java Main

This will run the four-thread demonstration first with the Filter Lock,
then with the Bakery Lock, printing the counter's expected and actual
final values after each run. A correct implementation will show:

    Expected: 20
    Actual:   20

for both locks (4 threads x 5 increments each).


4. FILTER LOCK - IMPLEMENTATION NOTES
--------------------------------------
Variables used:
  level[i]  - The highest level thread i is currently attempting to enter
              (0 to n-1). A higher level means the thread is closer to
              acquiring the lock.
  victim[l] - Identifies which thread most recently declared itself the
              "victim" (i.e. willing to wait) at level l.

How it works:
  A thread must pass through n-1 levels before entering the critical
  section. At each level l, the thread announces its intent by setting
  level[threadId] = l and then offers itself as the victim at that level.
  It then waits while there is at least one other thread at level >= l
  AND it is still the victim at that level. This guarantees that at most
  one thread can be "let through" a level at a time, and that at level
  n-1 only one thread remains, giving mutual exclusion.

  unlock() simply resets the thread's level back to 0, allowing other
  threads waiting at that level to proceed.


5. BAKERY LOCK - IMPLEMENTATION NOTES
----------------------------------------
Variables used:
  flag[i]  - Set to true while thread i is in the process of choosing a
             ticket number (label), and false otherwise. Used so that
             other threads do not read a half-updated label.
  label[i] - The ticket number (label) assigned to thread i. Threads are
             served in increasing order of label, with ties broken by
             thread ID.

How it works:
  A thread wishing to enter the critical section first raises its flag,
  computes a label one greater than the maximum label currently held by
  any thread, then lowers its flag again. It then waits for every other
  thread k to finish choosing its label (flag[k] == false) and for its
  own (label, threadId) pair to be strictly smaller than thread k's,
  guaranteeing a first-come-first-served order and mutual exclusion.

  unlock() resets the thread's label to 0, signalling that it has left
  the critical section and is no longer competing.


6. COMPARISON: FILTER LOCK vs BAKERY LOCK
-------------------------------------------
  - The Filter Lock uses a "level" based filtering mechanism where
    threads are progressively eliminated at each of the n-1 levels
    until only one remains. It relies on the victim variable to resolve
    conflicts between threads at the same level.
  - The Bakery Lock instead establishes a strict global ordering up
    front by having each thread draw a ticket (label), similar to a
    bakery queue system. Threads are served strictly in ticket order,
    which gives the Bakery Lock the additional property of being
    first-come-first-served (FCFS), which the Filter Lock does not
    guarantee.
  - Both algorithms avoid the need for hardware-level atomic
    instructions (such as compare-and-swap), relying only on reads and
    writes to volatile variables, but the Bakery Lock requires slightly
    more memory (an unbounded ticket counter in theory) and a full scan
    of all threads' labels on every lock() call.


7. CONCEPTUAL QUESTIONS
-------------------------
Conceptual Question 1:
  <Insert your group's answer here.>

Conceptual Question 2:
  <Insert your group's answer here.>


8. NOTES
--------
  - The supporting classes (Counter, VolatileInt, VolatileBoolean, Lock,
    Main, TaskGenerator) were provided as part of the practical and were
    not modified.
  - Both FilterLock.java and BakeryLock.java support an arbitrary number
    of threads n, specified via the constructor argument, and are not
    hard-coded to four threads (Main.java simply chooses to demonstrate
    with four).