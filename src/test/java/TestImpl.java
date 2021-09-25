import executore_manage.*;
import org.junit.jupiter.api.Test;

public class TestImpl {
    @Test
    public void test() throws InterruptedException {
        Runnable[] tasks = new Runnable[100];
        for (int i = 0; i < 100;++i) {
            int finalI = i;
            tasks[i] = () -> {
                try {
                    Thread.sleep((int)(1000*Math.random()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread " + finalI);
            };
        }
        tasks[4] = () ->{
            throw new RuntimeException();
        };

        tasks[19] = () ->{
            throw new RuntimeException();
        };
        Runnable callback = () -> {
            try {
                Thread.sleep((int)(1000*Math.random()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("I am Thread Callback");
        };
        ExecutionManager executionManager = new ExecuterManagerImpl();
        Thread.sleep(5000);
        Context context = executionManager.execute(callback, tasks);
        Thread.sleep(5000);
        System.out.println(context.getCompletedTaskCount());
        System.out.println(context.getFailedTaskCount());
        context.interrupt();
        System.out.println(context.getInterruptedTaskCount());
        Thread.sleep(15000);


    }
}
