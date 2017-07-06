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

/**
 * Created by zhaoshiqiang on 2017/6/6.
 */
public class Server {

    private final static int DEFAULT_IO_WORK_COUNT = 1;

    private ContextManager contextManager;
    private int port;
    private int processorNumber;
    private int ioWorkerNumber;
    private IoAcceptor acceptor;
    private volatile boolean terminated;
    private IRequestHandler requestHandler;
    private int maxQueueSize;
    private BlockingQueue<Runnable> requestQueue;
    private boolean verbose = false;

    public Server(int port, //服务端口
                  int ioWorkerNumber, //IO线程数
                  int processorNumber, //工作线程数
                  IRequestHandler handler,  //请求处理逻辑类
                  int maxQueueSize //服务可接受请求最大等待数目
    ){
        this.port = port;
        this.ioWorkerNumber = ioWorkerNumber;
        this.processorNumber = processorNumber;
        this.maxQueueSize = maxQueueSize;
    }

    public Server(int port,Object instance) throws IOException {
        acceptor = new SocketAcceptor();
        IoAcceptorConfig config = new SocketAcceptorConfig();
        DefaultIoFilterChainBuilder chain = config.getFilterChain();
        chain.addLast("logger",new LoggingFilter());
        chain.addLast("codec",new ProtocolCodecFilter(new WritableCodecFactory()));

        ThreadPoolExecutor executor = new ThreadPoolExecutor(1,1,0l, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(1));

        ServerHandler handler = new ServerHandler(executor,new RequestHandler(instance),contextManager);
        acceptor.bind(new InetSocketAddress(port), handler,config);
    }

    public Server(int port,IRequestHandler handler) throws IOException {
        acceptor = new SocketAcceptor();
        IoAcceptorConfig config = new SocketAcceptorConfig();
        DefaultIoFilterChainBuilder chain = config.getFilterChain();
        if (verbose){
            chain.addLast("logger",new LoggingFilter());
        }
        chain.addLast("codec",new ProtocolCodecFilter(new WritableCodecFactory()));

        ThreadPoolExecutor executor = new ThreadPoolExecutor(1,1,0l, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(1));
        if (handler instanceof IContextListener){
            contextManager = new ContextManager((IContextListener) handler);
        }else {
            contextManager = new ContextManager();
        }
        ServerHandler serverHandler = new ServerHandler(executor,handler,contextManager);
        acceptor.bind(new InetSocketAddress(port), serverHandler,config);
    }

    public void start(){
        terminated = false;
    }
    public void stop(){
        //取消所有监听
        acceptor.unbindAll();
        //关闭
        synchronized (this){
            terminated = true;
            this.notifyAll();
        }
    }
    public void join() throws InterruptedException {
        synchronized (this){
            while (!terminated){
                this.wait();
            }
        }
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public ContextManager getContextManager() {
        return contextManager;
    }
}
