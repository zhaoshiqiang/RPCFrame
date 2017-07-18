package client;

import odis.serialize.IWritable;

import java.util.concurrent.*;

/**
 * Created by zhaoshq on 2017/7/18.
 */
public class ExecutorCompletionService {

    private final BlockingQueue<BasicFuture> completionQueue;
    protected final ConnectionsManager connectionsManager;

    public ExecutorCompletionService(Executor executor, ConnectionsManager connectionsManager) {
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
