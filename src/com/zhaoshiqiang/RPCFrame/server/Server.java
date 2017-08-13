package com.zhaoshiqiang.RPCFrame.server;

import com.zhaoshiqiang.RPCFrame.commons.ExceptionWritable;
import com.zhaoshiqiang.RPCFrame.commons.NamedThreadFactory;
import com.zhaoshiqiang.RPCFrame.commons.WritableCodecFactory;
import com.zhaoshiqiang.RPCFrame.commons.DataPack;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于mina的服务器框架，可以通过非常简单的方法实现一个支持异步访问的服务器.服务器
 * 的主要逻辑通过{@link IRequestHandler}来实现，在已经实现了一个RequestHandler
 * 的情况下，通过如下代码就可以创建一个服务器实例:
 * <code>
 *   RequestHandler handler = ...;
 *   ...
 *   int concurrent = 5; // 处理请求的线程池大小为5
 *   Server com.zhaoshiqiang.server = new Server(port, concurrent, handler);
 *   com.zhaoshiqiang.server.start();
 * </code>
 * 需要注意的是，服务器的所有线程都是daemon线程，所以在没有其他线程的情况下，虚拟机会
 * 终止，可以通过如下代码一直运行服务器:
 * <code>
 *   com.zhaoshiqiang.server.join();
 * </code>
 *
 * Created by zhaoshiqiang on 2017/6/6.
 */
public class Server {

    private final static int DEFAULT_IO_WORK_COUNT = 1;
    private final static int DEFAULT_PROCESSOR_WORK_COUNT = 1;

    private ContextManager contextManager;
    private int port;
    private int processorNumber;
    private int ioWorkerNumber;
    private IoAcceptor acceptor;
    private volatile boolean terminated;
    private IRequestHandler requestHandler;
    private IContextListener contextListener;
    private int maxQueueSize;

    private boolean verbose = false;
    private RejectedExecutionHandler rejectedExecutionHandler;

    public Server(int port,Object instance){
        this(port,new RequestHandler(instance));
        if (instance instanceof IContextListener){
            contextListener = (IContextListener) instance;
        }else {
            contextListener = null;
        }
    }
    public Server(int port,Object instance,boolean verbose){
        this(port,DEFAULT_PROCESSOR_WORK_COUNT,DEFAULT_IO_WORK_COUNT,new RequestHandler(instance),-1,verbose,null);
    }
    public Server(int port, IRequestHandler handler) {
        this(port,DEFAULT_PROCESSOR_WORK_COUNT,DEFAULT_IO_WORK_COUNT,handler,-1,false,null);
    }

    public Server(int port, //服务端口
                  int processorNumber, //工作线程数
                  IRequestHandler handler,  //处理请求逻辑类
                  int maxQueueSize //服务可接受请求最大等待数目
    ){
        this(port,processorNumber,DEFAULT_IO_WORK_COUNT,handler,maxQueueSize,false,null);
    }

    public Server(int port, //服务端口
                  int ioWorkerNumber, //IO线程数
                  int processorNumber, //工作线程数
                  IRequestHandler handler,  //处理请求逻辑类
                  int maxQueueSize, //服务可接受请求最大等待数目
                  boolean verbose,
                  IContextListener contextListener
    ){
        this.port = port;
        this.ioWorkerNumber = ioWorkerNumber;
        this.processorNumber = processorNumber;
        this.maxQueueSize = maxQueueSize;
        this.requestHandler = handler;
        this.verbose = verbose;
        this.contextListener = contextListener;
    }

    /**
     * 启动服务器，开始监听指定的端口.
     * @throws IOException
     */
    public void start() throws IOException {
        terminated = false;
        //创建acceptor
        acceptor = new SocketAcceptor(ioWorkerNumber,new AcceptorExecutor());

        //准备配置
        SocketAcceptorConfig cfg = new SocketAcceptorConfig();
        //允许服务器在绑定到特定端口之前，先设置ServerSocket的一些选项。因为一旦服务器与特定端口绑定，有些选项就不能再改变了。
        cfg.setReuseAddress(true);
        if (verbose){
            cfg.getFilterChain().addLast("logger",new LoggingFilter());
        }
        cfg.getFilterChain().addLast("codec",new ProtocolCodecFilter(new WritableCodecFactory()));
        cfg.setThreadModel(ThreadModel.MANUAL);

        //接下来配置ThreadPoolExecutor
        BlockingQueue<Runnable> requestQueue;
        if (maxQueueSize == -1){
            requestQueue = new LinkedBlockingDeque<Runnable>();
        }else {
            requestQueue = new ArrayBlockingQueue<Runnable>(maxQueueSize);
        }
        if (rejectedExecutionHandler == null){
            rejectedExecutionHandler = new ServerRejectedExecutorHandler();
        }
        ThreadPoolExecutor processPoolExecutor = new ThreadPoolExecutor(
                processorNumber,
                processorNumber,
                0l,
                TimeUnit.MILLISECONDS,
                requestQueue,
                new NamedThreadFactory("processThread",true),
                rejectedExecutionHandler);

        if (contextListener != null){
            contextManager = new ContextManager(contextListener);
        }else {
            contextManager = new ContextManager();
        }
        ServerHandler serverHandler = new ServerHandler(processPoolExecutor,requestHandler,contextManager);
        acceptor.bind(new InetSocketAddress(port),serverHandler,cfg);
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

    /**
     * 设置是否记录连接的日志，默认情况下日志是打开的.
     * 这个设置只有在{@link #start()}调用以前才生效.
     * @param verbose
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    /**
     * 得到是否记录详细连接信息的开关.
     * @return
     */
    public boolean getVerbose() {
        return verbose;
    }

    public ContextManager getContextManager() {
        return contextManager;
    }

    /**
     * 这个executor为每个不同的ioworker线程起一个独立的名字
     */
    private class AcceptorExecutor implements Executor{
        private AtomicInteger id = new AtomicInteger(0);
        @Override
        public void execute(Runnable command) {
            Thread thread = new Thread(command, "ioworker_" + id.addAndGet(1));
            thread.setDaemon(true);
            thread.start();
        }
    }

    /**
     * 这个是server的饱和策略，当有界队列被填满，或者executor关闭的时候，饱和策略便会发挥作用
     */
    private class ServerRejectedExecutorHandler implements RejectedExecutionHandler{

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            RequestTask requestTask = (RequestTask) r;
            //这里需要把RejectedExecutionException包裹成IWritable作为结果返回
            DataPack respDataPack = new DataPack();
            respDataPack.setSeq(requestTask.getPack().getSeq());

            ExceptionWritable ew = new ExceptionWritable();
            ew.set(new RejectedExecutionException());
            respDataPack.add(ew);

            requestTask.getSession().write(respDataPack);
        }
    }
}
