package client;

/**
 * Created by zhaoshq on 2017/7/18.
 */
public class QueueingFuture extends BasicFuture {

    private final ExecutorCompletionService completionService;

    public QueueingFuture(ICallFinishListener listener, ExecutorCompletionService completionService) {
        super(listener);
        this.completionService = completionService;
    }

    public void setDone(Throwable invocationException, Object result){
        completionService.add(this);
        super.setDone(invocationException, result);
    }


}
