package task_callable;

/**
 * Exception in method, which has called. *
 */

public class CallableExceptionMy extends RuntimeException {
    CallableExceptionMy(String message) {
        super(message);
    }
    CallableExceptionMy(){
        super();
    }
}
