package server;

import commons.WritableCodecFactory;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoAcceptorConfig;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zhaoshiqiang on 2017/6/6.
 */
public class Server {

    private final static int DEFAULT_IO_WORK_COUNT = 1;

    private ContextManager contextManager = new ContextManager();
    private int port;
    private int processorNumber;
    private int ioWorkerNumber;
    private IoAcceptor acceptor;
    private AtomicBoolean terminated;
    private IRequestHandler requestHandler;
    private int mixQueueSize;
    private BlockingQueue<Runnable> requestQueue;

    public Server(int port,Object instance) throws IOException {
        IoAcceptor acceptor = new SocketAcceptor();
        IoAcceptorConfig config = new SocketAcceptorConfig();
        DefaultIoFilterChainBuilder chain = config.getFilterChain();
        chain.addLast("logger",new LoggingFilter());
        chain.addLast("codec",new ProtocolCodecFilter(new WritableCodecFactory()));

        ThreadPoolExecutor executor = new ThreadPoolExecutor(1,1,0l, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(1));

        ServerHandler handler = new ServerHandler(executor,new RequestHandler(instance),contextManager);
        acceptor.bind(new InetSocketAddress(port), handler,config);
    }

    public Server(int port,IRequestHandler handler) throws IOException {
        IoAcceptor acceptor = new SocketAcceptor();
        IoAcceptorConfig config = new SocketAcceptorConfig();
        DefaultIoFilterChainBuilder chain = config.getFilterChain();
        chain.addLast("logger",new LoggingFilter());
        chain.addLast("codec",new ProtocolCodecFilter(new WritableCodecFactory()));

        ThreadPoolExecutor executor = new ThreadPoolExecutor(1,1,0l, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(1));

        ServerHandler serverHandler = new ServerHandler(executor,handler,contextManager);
        acceptor.bind(new InetSocketAddress(port), serverHandler,config);
    }

    public void start(){

    }
    public void stop(){

    }
    public void join(){

    }

    public ContextManager getContextManager() {
        return contextManager;
    }
}
