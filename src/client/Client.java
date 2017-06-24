package client;

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

    private ConnectionsManager connectionsManager;

    public static Client getNewInstance(InetSocketAddress addr,int connectionCount){
        return getNewInstance(addr,connectionCount,DEFAULT_CONNECT_TIMEOUT,DEFAULT_WRITE_TIMEOUT,null,CallFuture.DefaultCallFutureFactory.instance);
    }

    public static Client getNewInstance(InetSocketAddress addr,ICallFutureFactory callFutureFactory){
        return getNewInstance(addr,1,DEFAULT_CONNECT_TIMEOUT,DEFAULT_WRITE_TIMEOUT,null,callFutureFactory);
    }
    public static Client getNewInstance(InetSocketAddress addr,int connectionCount, long connectTimeout,  long writeTimeout, ClientBasicHandler handler,ICallFutureFactory callFutureFactory){
       return new Client(connectionCount, connectTimeout, addr, writeTimeout, handler,callFutureFactory);
    }

    private Client(int connectionCount, long connectTimeout, InetSocketAddress addr, long writeTimeout, ClientBasicHandler handler,ICallFutureFactory callFutureFactory){
        connectionsManager = ConnectionsManager.getNewInstance(connectionCount, connectTimeout, addr, writeTimeout, handler,callFutureFactory);
    }

    /**
     * 提交一个请求，请求并不是立即完成的，请使用返回的{@link BasicFuture} 来得到call当前的状态.
     * @param objs
     * @param listener
     * @return
     */
    public Future submit(ICallFinishListener listener,IWritable... objs){
        if (!connectionsManager.getOpenedState()) {
            return null;
        }
        return connectionsManager.submit(listener,objs);
    }
    public boolean open() throws CallException {
        return connectionsManager.open();
    }

    public boolean close() throws CallException {
        return connectionsManager.close();
    }
}
