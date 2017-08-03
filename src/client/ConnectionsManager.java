package client;

import odis.serialize.IWritable;
import odis.serialize.lib.StringWritable;
import toolbox.misc.UnitUtils;
import toolbox.misc.concurrent.AtomicRotateInteger;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 这个类会被多个线程调用，需要考虑并发
 * Created by zhaoshq on 2017/6/21.
 */
public class ConnectionsManager {

    private static final long DEFAULT_BIND_TIMEOUT = 5 * UnitUtils.SECOND;

    private final int connectionCount;
    private final AtomicRotateInteger nextConnection;

    private final long connectTimeout;
    private final long writeTimeout;

    private final InetSocketAddress addr;

    private final ICallFutureFactory callFutureFactory;

    private List<Connection> connections = new ArrayList<Connection>();
    private final ReadWriteLock connlock = new ReentrantReadWriteLock();

    private AtomicBoolean opened = new AtomicBoolean(false);
    private String ctxKey = null;

    public static ConnectionsManager getNewInstance(int connectionCount, long connectTimeout, InetSocketAddress addr, long writeTimeout,ICallFutureFactory callFutureFactory){
        return new ConnectionsManager(connectionCount,connectTimeout,addr,writeTimeout, callFutureFactory);
    }

    private ConnectionsManager(int connectionCount, long connectTimeout, InetSocketAddress addr, long writeTimeout, ICallFutureFactory callFutureFactory) {
        this.connectionCount = connectionCount;
        this.connectTimeout = connectTimeout;
        this.addr = addr;
        this.writeTimeout = writeTimeout;
        nextConnection = new AtomicRotateInteger(0,connectionCount,0);
        this.callFutureFactory = callFutureFactory;
    }


    public Future submit(ICallFinishListener listener, IWritable... objs){
        //可以并发读，但是可以不能并发写
        connlock.readLock().lock();
        try {

            BasicFuture future = callFutureFactory.create(listener);
            if (getOpenedState()){
                Connection con = connections.get(nextConnection.getAndIncrement());
                if (!con.getClosed()){
                    //若该连接没有关闭则发送请求
                    return con.submit(future,objs);
                }else {
                    //若有一个连接关闭，则关闭全部连接
                    try {
                        close();
                    } catch (CallException e) {
                        /**
                         * 该异常只有在连接全部关闭之后，再调用{@link #close()}方法时才会抛出，
                         * 但是如果连接全部关闭，{@link #getOpenedState()}会返回false，根本不会进入到这里，
                         * 所以这个异常在这里是不会发生的，故而直接忽略。
                         * */
                    }
                    future.setDone(new ConnectionClosedException("connections are not opened yet !"),null);
                    return future;
                }
            }else {
                future.setDone(new ConnectionClosedException("connections are not opened yet !"),null);
                return future;
            }
        }finally {
            connlock.readLock().unlock();
        }
    }

    public boolean getOpenedState() {
        return opened.get();
    }

    /**
     * 创建指定数目的到服务器的连接，并且将所有这些连接都bind在 一个context上.
     *
     * @throws CallException
     */
    public boolean open(IHandlerListener handlerListener) throws CallException {
        //不能并发读，也不能并发写
        connlock.writeLock().lock();
        try {

            if (opened.get()){
                throw new CallException("client to " + addr + " is opened previously");
            }
            try {

                for (int i=0; i < connectionCount ; i++){
                    Connection conn = new Connection(addr,connectTimeout,writeTimeout);
                    conn.open(handlerListener);
                    connections.add(conn);
                    bindConnection(conn);
                }
                opened.set(true);
            }finally {
                //但凡有一个连接出错，则关闭connections中的所有连接
                if (!opened.get()){
                    for (Connection connection : connections){
                        connection.close();
                    }
                    connections.clear();
                }
            }

        }finally {
            connlock.writeLock().unlock();
        }

        return opened.get();
    }

    /**
     * 关闭所有的连接，所有没有完成或者返回的请求都会失败。
     */
    public boolean close() throws CallException {
        connlock.writeLock().lock();
        try {
            if (!opened.get()){
                throw new CallException("client to " + addr + " is closed previously");
            }
            for (Connection connection : connections){
                connection.close();
            }
            connections.clear();
            opened.set(false);
        }finally {
            connlock.writeLock().unlock();
        }
        return opened.get();
    }

    private void bindConnection(Connection con) throws CallException {
        if (ctxKey == null){
            //第一次将connection和context绑定在一起
            ctxKey = bindConnectionSubmit(con,new StringWritable());
            if (ctxKey.length() == 0){
                throw new CallException("bind connection to context failed : empty returned key");
            }
        }else {
            String key = bindConnectionSubmit(con,new StringWritable(ctxKey));
            if (!ctxKey.equals(key)){
                throw new CallException("bind connection to context failed : wrong returned key");
            }
        }
    }

    private String bindConnectionSubmit(Connection con,StringWritable keyString) throws CallException {
        BasicFuture future = callFutureFactory.create(null);
        con.submitWithId(future,-1,keyString);
        StringWritable result = null;
        try {
            result = (StringWritable) future.get(DEFAULT_BIND_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new CallException("bind call failed",e);
        } catch (ExecutionException e) {
            throw new CallException("bind call failed",e);
        } catch (TimeoutException e) {
            throw new CallTimeoutException("bind call timeout",e);
        }
        return result.get();
    }
}
