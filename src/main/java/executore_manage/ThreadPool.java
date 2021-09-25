package executore_manage;

public interface ThreadPool {
    void start();
    void execute(Runnable runnable);
    boolean isFinished();
}
