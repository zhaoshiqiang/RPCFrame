package client;

import commons.WritableCodecFactory;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by zhaoshq on 2017/6/1.
 */
public class Connection {
    private static final Logger LOGGER = LogFormatter.getLogger(Connection.class);
    private ConcurrentHashMap<Long, BasicFuture> callMap;
    private IoSession session;
    private boolean closed;
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
            callMap = new ConcurrentHashMap<Long, BasicFuture>();
            connectFuture = connector.connect(addr,new ClientBasicHandler(callMap),cfg);
        }else {
            callMap = handler.getCallMap();
            connectFuture = connector.connect(addr,handler,cfg);
        }

        connectFuture.join();
        this.session = connectFuture.getSession();
        this.session.setWriteTimeout((int) (writeTimeout/UnitUtils.SECOND));
        this.closed = false;
    }

    public IoSession getSession() {
        return session;
    }

    public Map<Long, BasicFuture> getCallMap() {
        return callMap;
    }
}
