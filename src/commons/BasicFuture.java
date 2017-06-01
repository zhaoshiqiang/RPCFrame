package commons;

import java.util.concurrent.*;

/**
 * Created by zhaoshq on 2017/6/1.
 */
public class BasicFuture implements Future {

    private static int CALLSTAT_RUNNING = 0;
    private static int CALLSTAT_FINISHED = 2;

    private CountDownLatch latch = new CountDownLatch(1);

    private Object result;
    private int status=CALLSTAT_RUNNING;
    private Throwable invocationException;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return status == CALLSTAT_FINISHED;
    }

    public void setDone(Throwable invocationException, Object result){
        if (status == CALLSTAT_RUNNING){
            status = CALLSTAT_FINISHED;
            this.invocationException = invocationException;
            this.result = result;
            latch.countDown();
        }
    }

    private Object getResult() throws ExecutionException {
        if (status == CALLSTAT_FINISHED){
            if (invocationException != null){
                throw new ExecutionException(invocationException);
            }else {
                return result;
            }
        }else {
            throw new RuntimeException("bad status" + status);
        }
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        latch.await();
        return getResult();
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        latch.await(timeout, unit);
        return getResult();
    }
}
