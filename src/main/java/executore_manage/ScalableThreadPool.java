package executore_manage;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Queue;

public class ScalableThreadPool implements ThreadPool {
    private Queue<Runnable> queueTasks;
    private LinkedList<Thread> currentThreads;
    private int nThreadsMin;
    private int nThreadsMax;
    private volatile Integer completedTasks = 0;
    private volatile Integer failedTasks = 0;
    volatile private Integer nThreadsFree;
    volatile private boolean stopped = false;
    final private Object monitorQueuetasksThreadsFree = new Object();
    final private Object monitorChangeCountThreads = new Object();



    private final Runnable daemon = () -> {
        while (true) {
            synchronized (monitorChangeCountThreads) {
                try {
                    monitorChangeCountThreads.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (monitorQueuetasksThreadsFree) {
                    //System.out.println("nThreads: " + currentThreads.size());
                    if (queueTasks.size() > nThreadsFree) {
                        int nAdd = Math.min(queueTasks.size() - nThreadsFree, nThreadsMax - currentThreads.size());
                        addThreads(nAdd);
                        //    System.out.println(" added thread's count : " + nAdd);
                    } else if (nThreadsFree > 0) {
                        //  System.out.println(" nThreadsFree : " + nThreadsFree);
                        int nDeleted = Math.min(nThreadsFree, currentThreads.size() - nThreadsMin);
                        removeThreads(nDeleted);
                        // System.out.println(" deleted thread's count : " + nDeleted);
                    }
                }
            }
        }
    };

    @Override
    public void start(){
        for (Thread thr : currentThreads)
            thr.start();
        synchronized (monitorChangeCountThreads){
            Thread daemonThread = new Thread(daemon);
            daemonThread.setDaemon(true);
            daemonThread.start();
        }
    }

    @Override
    public void execute(Runnable runnable) {
        if(!stopped)
            synchronized (monitorChangeCountThreads){
                synchronized (monitorQueuetasksThreadsFree) {
                    queueTasks.add(runnable);
                    monitorQueuetasksThreadsFree.notify();
                }
                monitorChangeCountThreads.notify();
            }
    }

    public void finish() throws InterruptedException {
        stopped = true;
        Thread thr;
        do{
            synchronized (monitorQueuetasksThreadsFree) {
                thr = currentThreads.isEmpty() ? null : currentThreads.poll();
            }
            synchronized (monitorChangeCountThreads) {
                monitorChangeCountThreads.notify();
            }
            if (thr != null) {
                if (thr.getState().equals(Thread.State.WAITING))
                    thr.interrupt();
                else
                    thr.join();

            }

        } while (thr != null);
    }

    public void interrupt() throws InterruptedException {
        synchronized (monitorChangeCountThreads) {
            synchronized (monitorQueuetasksThreadsFree) {
                queueTasks.clear();
            }
        }
        finish();
    }


    public ScalableThreadPool(int nThreadsMin, int nThreadsMax){
        if (nThreadsMin <= 0)
            throw new IllegalArgumentException("An argument cannot be less the  or equal it");
        if (nThreadsMin > nThreadsMax )
            throw new IllegalArgumentException("First argument must be less than second or equal to it!");
        queueTasks = new LinkedList<>();
        this.nThreadsMin = nThreadsMin;
        this.nThreadsMax = nThreadsMax;
        nThreadsFree = nThreadsMin;
        currentThreads = new LinkedList<>();
        for (int i = 0; i < nThreadsMin; ++i)
            currentThreads.add(new MyThread());
    }

    public boolean isFinished(){
        return stopped;
    }
    public int getSuccessTasks(){
        return completedTasks;
    }

    public int getFailedTasks(){
        return failedTasks;
    }

    private void addThreads(int n){
        for (int i = 0; i < n; ++i){
            MyThread thr = new MyThread();
            currentThreads.add(thr);
            thr.start();
        }
    }
    private void removeThreads(int n){
        if ( n<=0 )
            return;
        ListIterator<Thread> it = currentThreads.listIterator();
        while (n > 0 && it.hasNext()){
            Thread current = it.next();
            if (current.getState().equals(Thread.State.WAITING)) {
                current.interrupt();
                it.remove();
                n--;
            }
        }

    }
    class MyThread extends Thread {
        public void run() {
            boolean continueTasks = !stopped || !queueTasks.isEmpty();
            Runnable task = null;
            while (continueTasks) {
                synchronized (monitorChangeCountThreads) {
                    synchronized (monitorQueuetasksThreadsFree) {
                        if (!queueTasks.isEmpty()) {
                            task = queueTasks.poll();
                            nThreadsFree--;
                        }
                    }
                    monitorChangeCountThreads.notify();
                }
                if (task != null) {
                    boolean success = true;
                    try {
                        task.run();
                    } catch (RuntimeException exc) {
                        success = false;
                    }

                    if (success)
                        synchronized (completedTasks) {
                            completedTasks++;
                        }
                    else
                        synchronized (failedTasks) {
                            failedTasks++;
                        }
                }

                synchronized (monitorChangeCountThreads){
                    synchronized (monitorQueuetasksThreadsFree) {
                        if (task != null)
                            nThreadsFree++;
                        continueTasks = !stopped || !queueTasks.isEmpty();
                    }
                }
                task = null;
                if (continueTasks) {

                    synchronized (monitorQueuetasksThreadsFree) {
                        if (queueTasks.isEmpty())
                            try {
                                monitorQueuetasksThreadsFree.wait();
                            } catch (InterruptedException e) {
                                continueTasks = false;
                            }
                    }

                }
            }
        }
    }
}
