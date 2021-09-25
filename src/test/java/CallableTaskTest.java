import org.junit.jupiter.api.Test;
import task_callable.Task;
import task_callable.TaskImpl;
import java.util.concurrent.Callable;

public class CallableTaskTest {
    final Callable<Number> numberCallable = ()->{
        long l = (int)(Math.random()*10000);
        Thread.sleep(l);
        return l;
    };

    final Callable<String> stringCallable = ()->{
        long l = (int)(Math.random()*10000);
        Thread.sleep(l);
        return l + "string";
    };

    final Callable<String> stringCallable2 = ()->{
        long l = (int)(Math.random()*10000);
        Thread.sleep(l);
        throw new RuntimeException("stringCallable2");
        //return "string";
    };
    final Callable<String> stringCallable3 = ()->{
        long l = (int)(Math.random()*10000);
        Thread.sleep(l);
        return l + "string";
    };

    final Callable<Void> voidCallable = ()->{
        long l = (int)(Math.random()*10000);
        Thread.sleep(l);
        System.out.println("voidCallable ");
        return null;
    };

    @Test
    public void test(){
        Task<Number> t1= new TaskImpl<>(numberCallable);
        Task<String> t2= new TaskImpl<>(stringCallable);
        Task<Number> t3= new TaskImpl<>(numberCallable);
        Task<String> t4= new TaskImpl<>(stringCallable);
        Task<String> t5= new TaskImpl<>(stringCallable2);
        Task<String> t6= new TaskImpl<>(stringCallable3);
        Task<Void> t7= new TaskImpl<>(voidCallable);
        Thread thr1 = new Thread(()->{
            System.out.println("thr1 - t7 -" +t7.get());
            System.out.println("thr1 - t1 -" +t1.get());
            System.out.println("thr1 - t2 -" +t2.get());
            System.out.println("thr1 - t6 -" +t6.get());
            System.out.println("thr1 - t5 -" +t5.get());
            System.out.println("thr1 - t3 -" +t3.get());
            System.out.println("thr1- t4 -" +t4.get());
        });
        Thread thr2 = new Thread(()->{
            System.out.println("thr2 - t7 -" +t7.get());
            System.out.println("thr2 - t1 -" +t1.get());
            System.out.println("thr2 - t5 -" +t5.get());
            System.out.println("thr2 - t6 -" +t6.get());
            System.out.println("thr2 - t4 -" + t4.get());
            System.out.println("thr2 - t3 -" + t3.get());
            System.out.println("thr2 - t2 -" + t2.get());
            System.out.println("thr2 - t1 -" +t1.get());
        });
        thr1.start();
        thr2.start();

        try {
            thr1.join();
            thr2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    @Test
    public void test2(){
        Task<Number> t1= new TaskImpl<>(numberCallable);
        Task<String> t2= new TaskImpl<>(stringCallable);
        Task<Number> t3= new TaskImpl<>(numberCallable);
        Task<String> t4= new TaskImpl<>(stringCallable);

        Thread thr1 = new Thread(()->{
            System.out.println("thr1 - t1 -" +t1.get());
            System.out.println("thr1 - t2 -" +t2.get());
            System.out.println("thr1 - t3 -" +t3.get());
            System.out.println("thr1- t4 -" +t4.get());
        });
        Thread thr2 = new Thread(()->{
            System.out.println("thr2 - t1 -" +t1.get());
            System.out.println("thr2 - t2 -" + t2.get());
            System.out.println("thr2 - t3 -" + t3.get());
            System.out.println("thr2 - t4 -" + t4.get());
            System.out.println("thr2 - t1 -" +t1.get());
        });
        thr1.start();
        thr2.start();

        try {
            thr1.join();
            thr2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
