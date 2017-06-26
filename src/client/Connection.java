package client;

import commons.DataPack;
import commons.WritableCodecFactory;
import odis.serialize.IWritable;
import org.apache.mina.common.*;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.apache.mina.transport.socket.nio.SocketConnectorConfig;
import toolbox.misc.LogFormatter;
import toolbox.misc.UnitUtils;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 这个类会被多个线程调用，需要考虑并发
 * Created by zhaoshq on 2017/6/1.
 */
public class Connection {
    private static final Logger LOGGER = LogFormatter.getLogger(Connection.class);
    private final ConcurrentHashMap<Long, BasicFuture> callMap = new ConcurrentHashMap<Long, BasicFuture>();
    private IoSession session;
    private AtomicLong reqId = new AtomicLong(0);
    private volatile Boolean closed = false;

    //这里要改一下，最好不要在构造函数中启动线程，或者调用可改写的实例方法，否则会使this对象溢出
    Connection(InetSocketAddress addr, long connectTimeout, long writeTimeout, ClientBasicHandler handler){
        SocketConnector connector = new SocketConnector(1, new Executor() {
            @Override
            public void execute(Runnable command) {
                //设置这里启动的线程为deamon线程，这样通过SocketConnector启动的线程就全为deamon线程了
                Thread t = new Thread(command);
                t.setDaemon(true);
                t.start();
            }
        });

        SocketConnectorConfig cfg = new SocketConnectorConfig();
        int realConnectTimeout = (int) (connectTimeout/ UnitUtils.SECOND);
        if (realConnectTimeout <= 0){
            LOGGER.log(Level.WARNING,"minimal connect timeout is 1 second");
            realConnectTimeout = 1;
        }
        cfg.setConnectTimeout(realConnectTimeout);
        //设置编解码器
        connector.getFilterChain().addLast("codec",new ProtocolCodecFilter(new WritableCodecFactory()));
        //ThreadModel的作用其实就是在处理链的最后（handler之前）添加一个ExecutorFilter过滤器。
        cfg.setThreadModel(ThreadModel.MANUAL);
        ConnectFuture connectFuture = null;
        if (handler == null){
            ClientBasicHandler clientBasicHandler = new ClientBasicHandler();
            clientBasicHandler.setConnection(this);
            connectFuture = connector.connect(addr,clientBasicHandler ,cfg);
        }else {
            handler.setConnection(this);
            connectFuture = connector.connect(addr,handler,cfg);
        }

        connectFuture.join();
        this.session = connectFuture.getSession();
        this.session.setWriteTimeout((int) (writeTimeout/UnitUtils.SECOND));
        this.closed = false;
    }

    public Future submit(BasicFuture future, IWritable... objs){
        long id = reqId.addAndGet(1);
        return submitWithId(future, id, objs);
    }

    public Future submitWithId(BasicFuture future, long id, IWritable... objs) {

        //先检查，后执行
        synchronized (callMap){
            if (closed){
                future.setDone(new ConnectionClosedException("connection closed in previous call"),null);
                return future;
            }else {
                callMap.put(id,future);
            }
        }

        DataPack pack = new DataPack();
        pack.setSeq(id);
        for (IWritable obj : objs){
            pack.add(obj);
        }
        session.write(pack);
        return future;
    }

    public IoSession getSession() {
        return session;
    }

    public Map<Long, BasicFuture> getCallMap() {
        return callMap;
    }


    public Boolean getClosed() {
        return closed;
    }

    /**
     * 关闭连接，这里会关闭请求队列，并且将队列中的所有请求失败.
     */
    public void close() {
        this.closed = true;
        session.close();
    }
}
