package client;

import commons.DataPack;
import commons.ExceptionWritable;
import odis.serialize.IWritable;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import toolbox.misc.LogFormatter;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 这个类与{@link Connection}是一对一的关系，
 * 每次生成一个{@link Connection}时便要new一个{@link ClientHandler}与之对应
 * Created by zhaoshq on 2017/6/1.
 */
public class ClientHandler extends IoHandlerAdapter {

    public static final Logger LOG = LogFormatter.getLogger(Connection.class);
    private Connection connection;
    private final IHandlerListener handlerListener;

    public ClientHandler(IHandlerListener handlerListener) {
        this.handlerListener = handlerListener;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        DataPack pack = (DataPack) message;
        BasicFuture future = connection.getCallMap().remove(pack.getSeq());
        if (future == null){
            LOG.log(Level.WARNING,"connot find request for response with id" + pack.getSeq());
            return;
        }
        IWritable obj = pack.getFirst();
        //这里还要判断是否有异常
        if (obj instanceof ExceptionWritable){
            handlerListener.messageReceived(((ExceptionWritable) obj).get(),null);
            future.setDone(((ExceptionWritable) obj).get(),null);
        }else {
            handlerListener.messageReceived(null,obj);
            future.setDone(null,obj);
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        super.exceptionCaught(session, cause);
        handlerListener.exceptionCaught(cause);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {

        Map<Long, BasicFuture> callMap = connection.getCallMap();
        synchronized (callMap){
            Set<Long> keySet = callMap.keySet();
            //将请求队列中的等待结果全部失效
            for (Long key : keySet){
                BasicFuture future = callMap.get(key);
                future.setDone(new ConnectionClosedException("connection to " +session.getRemoteAddress() + " callMapclosed"),null);
                //将这个future移除
                callMap.remove(key);
            }
        }
        /**
         * 连接关闭后才会调用{@link #sessionClosed(IoSession)}，
         * 而连接关闭的方式除了客户端调用{@link Client#close()}}外，
         * 还可以通过方法调用使客户端主动关闭连接，而此时，closed这个状态是为true的
         */
        if (connection.getClosed()){
            connection.close();
        }
        handlerListener.sessionClosed();
    }

}
