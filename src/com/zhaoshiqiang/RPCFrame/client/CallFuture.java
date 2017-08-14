package com.zhaoshiqiang.RPCFrame.client;

/**
 * Created by zhaoshiqiang on 2017/6/13.
 */
public class CallFuture extends BasicFuture {
    protected long createTime = System.currentTimeMillis();

    public CallFuture(ICallFinishListener listener) {
        super(listener);
    }

    public long getCreateTime() {
        return createTime;
    }

    public enum DefaultCallFutureFactory implements ICallFutureFactory {
        instance;
        @Override
        public BasicFuture create(ICallFinishListener listener) {
            return new CallFuture(listener);
        }
    }
}
