package executore_manage;

import java.util.LinkedList;
import java.util.Queue;

public class FixedThreadPool implements ThreadPool {
    final private Queue<Runnable> queueTasks = new LinkedList<>();
    final private Thread[] currentThreads;
    volatile private boolean stopped = false;
    Runnable runnable = ()->{
        boolean continueTasks = !stopped || !queueTasks.isEmpty();
        Runnable task = null;
        while (continueTasks){
            synchronized (queueTasks) {
                if (!queueTasks.isEmpty()) {
                    task = queueTasks.poll();
                }
            }
            if (task != null) {
                task.run();
                task = null;
            }
            synchronized (queueTasks) {
                continueTasks = !stopped || !queueTasks.isEmpty();
            }
            if (continueTasks) {
                synchronized (queueTasks) {
                    if (queueTasks.isEmpty())
                        try {
                            queueTasks.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                }
            }
        }
    };


    @Override
    public void start(){
        for (Thread thr: currentThreads)
            thr.start();
    }

    @Override
    public void execute(Runnable runnable) {
        if(!stopped)
            synchronized (queueTasks) {
                queueTasks.add(runnable);
                queueTasks.notify();
            }
    }

    public void finish() throws InterruptedException {
        stopped = true;
        // System.out.println("FINISH " + queueTasks.size());
        for (Thread thr : currentThreads)
            thr.join();
    }

    public boolean isFinished(){
        return stopped;
    }

    public void interrupt() throws InterruptedException {
        synchronized (queueTasks){
            queueTasks.clear();
        }
        finish();
    }

    public FixedThreadPool(int nThreads){
        currentThreads = new Thread[nThreads];
        for(int i = 0; i < nThreads; ++i)
            currentThreads[i] = new Thread(runnable);
    }
}
