package client;

import commons.BasicFuture;
import commons.DataPack;
import odis.serialize.IWritable;
import org.apache.mina.common.IoHandler;
import toolbox.misc.UnitUtils;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zhaoshiqiang on 2017/6/1.
 */
public class Client {

    public static final long DEFAULT_WRITE_TIMEOUT = 10 * UnitUtils.SECOND;
    public static final long DEFAULT_CONNECT_TIMEOUT = 10 * UnitUtils.SECOND;

    private Connection connection;

    public static Client getNewInstance(InetSocketAddress addr){
        return getNewInstance(addr,DEFAULT_CONNECT_TIMEOUT,DEFAULT_WRITE_TIMEOUT);
    }

    public static Client getNewInstance(InetSocketAddress addr, long connectTimeout, long writeTimeout){
       return new Client(addr,connectTimeout,writeTimeout,null);
    }

    private Client(InetSocketAddress addr, long connectTimeout, long writeTimeout, IoHandler ioHandler){
        connection = new Connection(addr, connectTimeout, writeTimeout, ioHandler);
    }
    private AtomicLong reqId = new AtomicLong(0);

    /**
     * 提交一个请求，请求并不是立即完成的，请使用返回的{@link BasicFuture} 来得到call当前的状态.
     * @param objs
     * @return
     */
    public Future submit(IWritable... objs){
        long id = reqId.addAndGet(1);
        BasicFuture future = new BasicFuture();
        connection.getCallMap().put(id,future);

        DataPack pack = new DataPack();
        pack.setSeq(id);
        for (IWritable obj : objs){
            pack.add(obj);
        }
        connection.getSession().write(pack);
        return future;
    }
    public void close(){
        connection.getSession().close();
    }
}
