package com.zhaoshiqiang.RPCFrame.client;

/**
 * 创建Future对象的工厂接口，其实现一般为单例. 在框架的实现中，支持基本的数据传输支持，rpc访问接口支持，并且
 * 所有这些接口都支持异步访问，所以，我们用一个factory来维持这些访问层次中一致的{@link BasicFuture}类型.
 *
 * Created by zhaoshiqiang on 2017/6/13.
 */
public interface ICallFutureFactory {
    BasicFuture create(ICallFinishListener listener);
}
