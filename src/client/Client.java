package client;

import commons.DataPack;
import odis.serialize.IWritable;
import toolbox.misc.UnitUtils;

import java.net.InetSocketAddress;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zhaoshiqiang on 2017/6/1.
 */
public class Client {

    public static final long DEFAULT_WRITE_TIMEOUT = 10 * UnitUtils.SECOND;
    public static final long DEFAULT_CONNECT_TIMEOUT = 10 * UnitUtils.SECOND;

    private Connection connections;
    private AtomicLong reqId = new AtomicLong(0);
    private ICallFutureFactory callFutureFactory = CallFuture.DefaultCallFutureFactory.instance;

    public void setCallFutureFactory(ICallFutureFactory callFutureFactory) {
        this.callFutureFactory = callFutureFactory;
    }

    public static Client getNewInstance(InetSocketAddress addr){
        return getNewInstance(addr,DEFAULT_CONNECT_TIMEOUT,DEFAULT_WRITE_TIMEOUT);
    }

    public static Client getNewInstance(InetSocketAddress addr, long connectTimeout, long writeTimeout){
       return new Client(addr,connectTimeout,writeTimeout,null);
    }

    private Client(InetSocketAddress addr, long connectTimeout, long writeTimeout, ClientBasicHandler Handler){
        connections = new Connection(addr, connectTimeout, writeTimeout, Handler);
    }


    /**
     * 提交一个请求，请求并不是立即完成的，请使用返回的{@link BasicFuture} 来得到call当前的状态.
     * @param objs
     * @param listener
     * @return
     */
    public Future submit(ICallFinishListener listener,IWritable... objs){
        long id = reqId.addAndGet(1);
        BasicFuture future = callFutureFactory.create(listener);
        connections.getCallMap().put(id,future);

        DataPack pack = new DataPack();
        pack.setSeq(id);
        for (IWritable obj : objs){
            pack.add(obj);
        }
        connections.getSession().write(pack);
        return future;
    }
    public void close(){
        connections.getSession().close();
    }
}
