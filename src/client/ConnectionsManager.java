package client;

import commons.DataPack;
import odis.serialize.IWritable;
import org.apache.mina.common.IoHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zhaoshq on 2017/6/21.
 */
public class ConnectionsManager {
    private final int connectionCount;

    private final long connectTimeout;

    private final InetSocketAddress addr;

    private final long writeTimeout;

    private final IoHandler handler;

    private AtomicLong reqId = new AtomicLong(0);

    public ConnectionsManager(int connectionCount, long connectTimeout, InetSocketAddress addr, long writeTimeout, IoHandler handler) {
        this.connectionCount = connectionCount;
        this.connectTimeout = connectTimeout;
        this.addr = addr;
        this.writeTimeout = writeTimeout;
        this.handler = handler;
    }

    public ConnectionsManager(int connectionCount, long connectTimeout, InetSocketAddress addr, long writeTimeout) {
        this.connectionCount = connectionCount;
        this.connectTimeout = connectTimeout;
        this.addr = addr;
        this.writeTimeout = writeTimeout;
        this.handler = null;
    }

//    public Future submit(ICallFinishListener listener, IWritable... objs){
//        long id = reqId.addAndGet(1);
//        BasicFuture future = callFutureFactory.create(listener);
//        connections.getCallMap().put(id,future);
//
//        DataPack pack = new DataPack();
//        pack.setSeq(id);
//        for (IWritable obj : objs){
//            pack.add(obj);
//        }
//        connections.getSession().write(pack);
//        return future;
//    }
}
