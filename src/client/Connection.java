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

    private final InetSocketAddress addr;
    private final long connectTimeout;
    private final long writeTimeout;

    private final ConcurrentHashMap<Long, BasicFuture> callMap = new ConcurrentHashMap<Long, BasicFuture>();
    private IoSession session;
    private AtomicLong reqId = new AtomicLong(0);
    private volatile Boolean closed;

    Connection(InetSocketAddress addr, long connectTimeout, long writeTimeout){
        this.addr = addr;
        this.connectTimeout = connectTimeout;
        this.writeTimeout = writeTimeout;
    }

    public void open(IHandlerListener handlerListener){

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
        //新建一个handler与此connecttion对应
        ClientHandler clientHandler = new ClientHandler(handlerListener);
        clientHandler.setConnection(this);
        connectFuture = connector.connect(addr, clientHandler,cfg);
        //等待子线程执行完毕之后再执行，将异步执行的线程合并为同步
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

        try {
            session.write(pack);
        }catch (Throwable e){
            future.setDone(new CallException("request not submitted for unexpected exception", e), null);
        }
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
