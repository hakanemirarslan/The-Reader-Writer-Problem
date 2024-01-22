package sync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class Main {
    public static void main(String [] args) throws InterruptedException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        ReadWriteLock RW = new ReadWriteLock();


        executorService.execute(new Writer(RW));
        executorService.execute(new Writer(RW));
        executorService.execute(new Writer(RW));
        executorService.execute(new Writer(RW));

        executorService.execute(new Reader(RW));
        executorService.execute(new Reader(RW));
        executorService.execute(new Reader(RW));
        executorService.execute(new Reader(RW));


        executorService.shutdown();
    }
}
class ReadWriteLock{
    private Semaphore S = new Semaphore(1);
    private Semaphore S2 = new Semaphore(1);
    private Semaphore writer = new Semaphore(0);

    int activeReadersCount = 0;
    int completedReadersCount = 0;
    boolean isWriterActive = false;

    public void readLock() throws InterruptedException {
        S.acquire();
        activeReadersCount++;
        S.release();
    }

    public void writeLock() throws InterruptedException {
        S.acquire();
        S2.acquire();
        if(activeReadersCount == completedReadersCount){
            S2.release();
        }else{
            isWriterActive = true;
            S2.release();
            writer.acquire();
            isWriterActive = false;
        }
    }

    public void readUnLock() throws InterruptedException {
        S2.acquire();
        completedReadersCount++;
        if(isWriterActive && activeReadersCount == completedReadersCount) {
            writer.release();
        }
        S2.release();
    }
    public void writeUnLock() throws InterruptedException {
        S.release();
    }

};

class Writer implements Runnable {
    private ReadWriteLock RW_lock;

    public Writer(ReadWriteLock rw) {
        RW_lock = rw;
    }

    public void run() {
        while (true){
            try {
                RW_lock.writeLock();
                System.out.println("Writer Thread "+Thread.currentThread().getName() + " is Writting");
                Thread.sleep(2000);
                System.out.println("Writer Thread "+Thread.currentThread().getName() + " has finished Writting");
                RW_lock.writeUnLock();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Reader implements Runnable {
    private sync.ReadWriteLock RW_lock;

    public Reader(sync.ReadWriteLock rw) { RW_lock = rw; }

    public void run() {
        while (true) {
            try {
                RW_lock.readLock();
                System.out.println("Read Thread "+Thread.currentThread().getName() + " is Reading");
                Thread.sleep(1500);
                System.out.println("Read Thread "+Thread.currentThread().getName() + " has Finished Reading");
                RW_lock.readUnLock();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}


