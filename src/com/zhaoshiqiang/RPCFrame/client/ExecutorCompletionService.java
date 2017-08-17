package com.zhaoshiqiang.RPCFrame.client;

import odis.serialize.IWritable;

import java.util.concurrent.*;

/**
 *  提交一组任务，并且在任务完成后获取结果。
 * （可以保留与每个任务关联的Future，然后反复使用get方法，
 * 同时将参数timeout指定为0，从而通过轮询来判断任务是否完成。
 * 这种方法虽然可行，但是却繁琐。）
 * {@link ExecutorCompletionService} 便是这类问题的解决方案：
 * {@link ExecutorCompletionService}{@link ConnectionsManager} 和{@link BlockingQueue}融合在一起
 * 将一组任务提交给{@link ExecutorCompletionService}来执行，
 * 然后使用类似于队列的{@link BlockingQueue {@link #take()}}和{@link BlockingQueue {@link #poll()}}等方法来获取已完成的结果，
 * 注意：
 *      若使用{@link ExecutorCompletionService}的功能，需要将{@link QueueingFuture}作为Future。
 *      也可以单独地调用submit方法，当作单一任务来提交处理
 * Created by zhaoshq on 2017/7/18.
 */
public class ExecutorCompletionService {

    private final BlockingQueue<BasicFuture> completionQueue;
    protected final ConnectionsManager connectionsManager;

    public ExecutorCompletionService(ConnectionsManager connectionsManager) {
        this.connectionsManager = connectionsManager;
        this.completionQueue = new LinkedBlockingDeque<>();
    }

    public Future submit(ICallFinishListener listener, IWritable... objs){
        return connectionsManager.submit(listener, objs);
    }

    public boolean getOpenedState(){
        return connectionsManager.getOpenedState();
    }

    public boolean open(IHandlerListener handlerListener) throws CallException{
        return connectionsManager.open(handlerListener);
    }

    public boolean close() throws CallException{
        return connectionsManager.close();
    }

    /**
     * 需要在{@link BasicFuture#setDone(Throwable, Object)}中调用此方法，
     * 否则用户无法获得一组任务完成后的结果
     * @param future
     */
    public void add(BasicFuture future){
        completionQueue.add(future);
    }

    public Future take() throws InterruptedException {
        return completionQueue.take();
    }


    public Future poll() {
        return completionQueue.poll();
    }


    public Future poll(long timeout, TimeUnit unit) throws InterruptedException {
        return completionQueue.poll(timeout, unit);
    }
}
