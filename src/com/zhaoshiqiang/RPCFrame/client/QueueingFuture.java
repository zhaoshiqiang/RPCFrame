package com.zhaoshiqiang.RPCFrame.client;

/**
 * Created by zhaoshq on 2017/7/18.
 */
public class QueueingFuture extends CallFuture {

    private final ExecutorCompletionService completionService;

    public QueueingFuture(ICallFinishListener listener, ExecutorCompletionService completionService) {
        super(listener);
        this.completionService = completionService;
    }
    @Override
    public void setDone(Throwable invocationException, Object result){
        completionService.add(this);
        super.setDone(invocationException, result);
    }

    public enum QueueingFutureFactory implements ICallFutureFactory {
        instance;
        private ExecutorCompletionService completionService;
        @Override
        public BasicFuture create(ICallFinishListener listener) {
            return new QueueingFuture(listener,completionService);
        }

        public void setCompletionService(ExecutorCompletionService completionService) {
            this.completionService = completionService;
        }
    }
}
