package executore_manage;

public class ExecuterManagerImpl implements ExecutionManager{
    private volatile boolean interrupted = false;
    private int nTasks;
    private ScalableThreadPool threadPool;

    private void startLogic(Runnable callback, Runnable... tasks) {
        for (Runnable task : tasks)
            threadPool.execute(task);
            threadPool.start();
            while (!interrupted && threadPool.getSuccessTasks()+ threadPool.getFailedTasks() != nTasks){

            }

        try {
            threadPool.finish();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        callback.run();
        Thread.currentThread().interrupt();
    }
    @Override
    public Context execute(Runnable callback, Runnable... tasks){
        nTasks = tasks.length;
        threadPool = new ScalableThreadPool(1,16);
        Thread thr = new Thread(()-> startLogic(callback, tasks));
        thr.start();
        return new ContextImpl();
    }



    class ContextImpl implements Context{
       public int getCompletedTaskCount(){
            return threadPool.getSuccessTasks();
        }

       public int getFailedTaskCount(){
            return threadPool.getFailedTasks();
        }

        public int getInterruptedTaskCount() {
           if (!interrupted)
               throw new IllegalCallerException();
           return nTasks - getFailedTaskCount() - getCompletedTaskCount();
        }

        public void interrupt(){
            try {
                threadPool.interrupt();
                interrupted = true;
            } catch (InterruptedException exc){
                System.out.println(exc.getStackTrace());
            }

        }

       public boolean isFinished(){
           return threadPool.isFinished();
        }

    }



}
