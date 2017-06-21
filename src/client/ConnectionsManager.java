package client;

import commons.DataPack;
import odis.serialize.IWritable;
import toolbox.misc.concurrent.AtomicRotateInteger;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zhaoshq on 2017/6/21.
 */
public class ConnectionsManager {
    private final int connectionCount;
    private final AtomicRotateInteger nextConnection;

    private final long connectTimeout;
    private final long writeTimeout;

    private final InetSocketAddress addr;

    private final ClientBasicHandler handler;

    private final ICallFutureFactory callFutureFactory;

    private AtomicLong reqId = new AtomicLong(0);
    private List<Connection> connections;
    private AtomicBoolean opened = new AtomicBoolean(false);

    public static ConnectionsManager getNewInstance(int connectionCount, long connectTimeout, InetSocketAddress addr, long writeTimeout, ClientBasicHandler handler,ICallFutureFactory callFutureFactory){
        return new ConnectionsManager(connectionCount,connectTimeout,addr,writeTimeout,handler, callFutureFactory);
    }

    private ConnectionsManager(int connectionCount, long connectTimeout, InetSocketAddress addr, long writeTimeout, ClientBasicHandler handler, ICallFutureFactory callFutureFactory) {
        this.connectionCount = connectionCount;
        this.connectTimeout = connectTimeout;
        this.addr = addr;
        this.writeTimeout = writeTimeout;
        this.handler = handler;
        nextConnection = new AtomicRotateInteger(0,connectionCount,0);
        this.callFutureFactory = callFutureFactory;
    }



    public Future submit(ICallFinishListener listener, IWritable... objs){
        long id = reqId.addAndGet(1);
        BasicFuture future = callFutureFactory.create(listener);
        Connection con = connections.get(nextConnection.getAndIncrement());
        con.getCallMap().put(id,future);

        DataPack pack = new DataPack();
        pack.setSeq(id);
        for (IWritable obj : objs){
            pack.add(obj);
        }
        con.getSession().write(pack);
        return future;
    }

    public boolean getOpenedState() {
        return opened.get();
    }

    public boolean open(){
        return opened.get();
    }

    public boolean close(){
        return opened.get();
    }
}
