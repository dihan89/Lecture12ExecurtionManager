package executore_manage;

public interface ExecutionManager {
    Context execute(Runnable callback, Runnable... tasks);
}
