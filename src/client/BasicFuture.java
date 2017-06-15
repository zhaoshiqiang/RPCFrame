package client;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基本的future实现，实现主要包括如下的部分：
 * <ul>
 * <li> {@link Future} 的基本访问接口，包括同步接口和结果访问接口</li>
 * <li> 实际任务的数据反馈接口：{@link #setDone(Throwable, Object)}.
 * <li> 对于任务回调的支持.
 * </ul>
 *
 * 这个实现不支持 {@link #cancel(boolean)}，所以， {@link #cancel(boolean)}
 * 和 {@link #isCancelled()} 方法都是直接返回false.
 *
 * 这个是Future的基本适配器，用户可以根据实际情况重写其中的方法
 * Created by zhaoshq on 2017/6/1.
 */
public class BasicFuture implements Future {

    private static int CALLSTAT_RUNNING = 0;
    private static int CALLSTAT_FINISHED = 2;

    private CountDownLatch latch = new CountDownLatch(1);

    private Object result;
    //之后如果支持cancel，那么其他线程会去修改这个状态，所以需要是线程安全的
    private AtomicInteger status=new AtomicInteger(CALLSTAT_RUNNING);
    private Throwable invocationException;
    private ICallFinishListener listener;

    public BasicFuture(ICallFinishListener listener) {
        this.listener = listener;
    }

    /**
     * 这个future实现不支持cancel，所以本方法直接返回false.
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }
    /**
     * 当前实现不支持cancel，所以，这个方法一定返回false.
     */
    @Override
    public boolean isCancelled() {
        return false;
    }

    /**
     * 判断当前的请求是否已经完成，如果这个方法返回true，后续对于 {@link #get()}的调用
     * 一定立即返回.
     */
    @Override
    public boolean isDone() {
        return status.get() == CALLSTAT_FINISHED;
    }

    /**
     * 设置任务的返回数据
     * @param invocationException
     * @param result
     */
    public void setDone(Throwable invocationException, Object result){
        if (status.compareAndSet(CALLSTAT_RUNNING,CALLSTAT_FINISHED)){
            this.invocationException = invocationException;
            this.result = result;
            latch.countDown();
            if (listener != null){
                listener.onFinish(this);
            }
        }
    }

    /**
     * 在当前future的状态是已完成的情况下，根据保存的结果数据和exception信息构造返回值.
     * @return
     * @throws ExecutionException
     */
    private Object getResult() throws ExecutionException {
        if (status.get() == CALLSTAT_FINISHED){
            if (invocationException != null){
                throw new ExecutionException(invocationException);
            }else {
                return result;
            }
        }else {
            throw new RuntimeException("bad status" + status.get());
        }
    }

    /**
     * 等待请求返回，并且返回请求的返回值. 请求过程中发生的exception都被保存在
     * {@link ExecutionException} 中抛出.
     */
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
