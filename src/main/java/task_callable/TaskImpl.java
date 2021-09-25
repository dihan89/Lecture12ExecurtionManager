package task_callable;
import java.util.Optional;
import java.util.concurrent.Callable;

public class TaskImpl<T> implements Task<T> {
    private static final java.util.concurrent.ConcurrentMap<Callable<?>, Optional> map
            = new java.util.concurrent.ConcurrentHashMap<>();
    private final Callable<T> callable;
    public TaskImpl(Callable<? extends T> callable){
        this.callable = (Callable<T>) callable;
    }

    public T get() {
        if (map.containsKey((callable))){
            System.out.println("FROM CACHE: ");
            Optional<?> optional = map.get(callable);
            Object obj = optional.isEmpty()? null : optional.get();
            if (obj != null && obj.getClass() == CallableExceptionMy.class)
                throw (CallableExceptionMy) obj;
            return (T) obj;
        }
        synchronized (callable){
            if (map.containsKey((callable))){
                System.out.println("FROM CACHE: ");
                Optional<?> optional = map.get(callable);
                Object obj = optional.isEmpty()? null : optional.get();
                if (obj != null && obj.getClass() == CallableExceptionMy.class)
                    throw (CallableExceptionMy) obj;
                return (T) obj;
            }
            try {
                T result = callable.call();
                Optional<T> optional = (result == null) ? Optional.empty() : Optional.of(result);
                map.put(callable, optional);
                return result;
            } catch (Exception e) {
                CallableExceptionMy exc = new CallableExceptionMy(
                        String.format("Exception in  task %s", callable.getClass()));
                map.put(callable, Optional.of(exc));
                throw exc;
            }
        }
    }
}
