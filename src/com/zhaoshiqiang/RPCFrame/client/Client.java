package com.zhaoshiqiang.RPCFrame.client;

import odis.serialize.IWritable;
import toolbox.misc.UnitUtils;

import java.net.InetSocketAddress;
import java.util.concurrent.Future;

/**
 * Created by zhaoshiqiang on 2017/6/1.
 */
public class Client {

    public static final long DEFAULT_WRITE_TIMEOUT = 10 * UnitUtils.SECOND;
    public static final long DEFAULT_CONNECT_TIMEOUT = 10 * UnitUtils.SECOND;

    protected ExecutorCompletionService completionService;

    public static Client getNewInstance(InetSocketAddress addr,int connectionCount){
        return getNewInstance(addr,connectionCount,DEFAULT_CONNECT_TIMEOUT,DEFAULT_WRITE_TIMEOUT,CallFuture.DefaultCallFutureFactory.instance);
    }

    public static Client getNewInstance(InetSocketAddress addr,ICallFutureFactory callFutureFactory){
        return getNewInstance(addr,1,DEFAULT_CONNECT_TIMEOUT,DEFAULT_WRITE_TIMEOUT,callFutureFactory);
    }
    public static Client getNewInstance(InetSocketAddress addr,ICallFutureFactory callFutureFactory,int connectionCount){
        return getNewInstance(addr,connectionCount,DEFAULT_CONNECT_TIMEOUT,DEFAULT_WRITE_TIMEOUT,callFutureFactory);
    }
    public static Client getNewInstance(InetSocketAddress addr, int connectionCount, long connectTimeout, long writeTimeout, ICallFutureFactory callFutureFactory){
       return new Client(connectionCount, connectTimeout, addr, writeTimeout,callFutureFactory);
    }

    protected Client(int connectionCount, long connectTimeout, InetSocketAddress addr, long writeTimeout, ICallFutureFactory callFutureFactory){

        if (callFutureFactory instanceof QueueingFuture.QueueingFutureFactory){
            ((QueueingFuture.QueueingFutureFactory)callFutureFactory).setCompletionService(completionService);
        }
        completionService = new ExecutorCompletionService(ConnectionsManager.getNewInstance(connectionCount, connectTimeout, addr, writeTimeout,callFutureFactory));
    }

    /**
     * 提交一个请求，请求并不是立即完成的，请使用返回的{@link BasicFuture} 来得到call当前的状态.
     * @param objs
     * @param listener
     * @return
     */
    public Future submit(ICallFinishListener listener,IWritable... objs) throws ConnectionClosedException {
        //如果连接是关闭的，则直接返回
        if (!completionService.getOpenedState()) {
            throw new ConnectionClosedException("connections are not opened yet !");
        }
        return completionService.submit(listener,objs);
    }

    public boolean open() throws CallException {
        return completionService.open(new HandlerListener(this));
    }
    public boolean isOpened(){
        return completionService.getOpenedState();
    }
    /**
     * 客户端主动关闭连接，所有没有完成或者返回的请求都会失败。
     * @return
     * @throws CallException
     */
    public boolean close() throws CallException {
        return completionService.close();
    }
}
