package client;

import odis.serialize.IWritable;
import org.apache.mina.common.IdleStatus;
import toolbox.misc.UnitUtils;

import java.net.InetSocketAddress;
import java.util.concurrent.Future;

/**
 * Created by zhaoshiqiang on 2017/6/1.
 */
public class Client implements IHandlerListener {

    public static final long DEFAULT_WRITE_TIMEOUT = 10 * UnitUtils.SECOND;
    public static final long DEFAULT_CONNECT_TIMEOUT = 10 * UnitUtils.SECOND;

    protected ConnectionsManager connectionsManager;
    protected IHandlerListener handlerListener;

    public static Client getNewInstance(InetSocketAddress addr,int connectionCount){
        return getNewInstance(addr,connectionCount,DEFAULT_CONNECT_TIMEOUT,DEFAULT_WRITE_TIMEOUT,CallFuture.DefaultCallFutureFactory.instance);
    }

    public static Client getNewInstance(InetSocketAddress addr,ICallFutureFactory callFutureFactory){
        return getNewInstance(addr,1,DEFAULT_CONNECT_TIMEOUT,DEFAULT_WRITE_TIMEOUT,callFutureFactory);
    }
    public static Client getNewInstance(InetSocketAddress addr, int connectionCount, long connectTimeout, long writeTimeout, ICallFutureFactory callFutureFactory){
       return new Client(connectionCount, connectTimeout, addr, writeTimeout,callFutureFactory);
    }

    protected Client(int connectionCount, long connectTimeout, InetSocketAddress addr, long writeTimeout, ICallFutureFactory callFutureFactory){
        connectionsManager = ConnectionsManager.getNewInstance(connectionCount, connectTimeout, addr, writeTimeout,callFutureFactory);
    }

    /**
     * 提交一个请求，请求并不是立即完成的，请使用返回的{@link BasicFuture} 来得到call当前的状态.
     * @param objs
     * @param listener
     * @return
     */
    public Future submit(ICallFinishListener listener,IWritable... objs){
        //如果连接是关闭的，则直接返回
        if (!connectionsManager.getOpenedState()) {
            return null;
        }
        return connectionsManager.submit(listener,objs);
    }

    public boolean open() throws CallException {
        return connectionsManager.open(new HandlerListener(this));
    }

    public boolean isOpened(){
        return connectionsManager.getOpenedState();
    }
    /**
     * 客户端主动关闭连接，所有没有完成或者返回的请求都会失败。
     * @return
     * @throws CallException
     */
    public boolean close() throws CallException {
        return connectionsManager.close();
    }

    @Override
    public void sessionCreated() throws Exception {

    }

    @Override
    public void sessionOpened() throws Exception {

    }

    @Override
    public void sessionClosed() throws Exception {

    }

    @Override
    public void sessionIdle(IdleStatus var1) throws Exception {

    }

    @Override
    public void exceptionCaught(Throwable var1) throws Exception {

    }

    @Override
    public void messageReceived(Throwable var1, Object var2) throws Exception {

    }

    @Override
    public void messageSent(Object var1) throws Exception {

    }
}
