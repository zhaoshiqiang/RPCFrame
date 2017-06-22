package client;

import commons.DataPack;
import commons.ExceptionWritable;
import odis.serialize.IWritable;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import toolbox.misc.LogFormatter;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by zhaoshq on 2017/6/1.
 */
public class ClientBasicHandler extends IoHandlerAdapter {

    public static final Logger LOG = LogFormatter.getLogger(Connection.class);
    private final ConcurrentHashMap<Long, BasicFuture> callMap;
    private Boolean closed;

    public ClientBasicHandler(ConcurrentHashMap<Long, BasicFuture> callMap) {
        this.callMap = callMap;
    }

    public Boolean getClosed() {
        return closed;
    }

    public void setClosed(Boolean closed) {
        this.closed = closed;
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        DataPack pack = (DataPack) message;
        BasicFuture future = callMap.remove(pack.getSeq());
        if (future == null){
            LOG.log(Level.WARNING,"connot find request for response with id" + pack.getSeq());
            return;
        }
        IWritable obj = pack.getFirst();
        //这里还要判断是否有异常
        if (obj instanceof ExceptionWritable){
            future.setDone(((ExceptionWritable) obj).get(),null);
        }else {
            future.setDone(null,obj);
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        super.exceptionCaught(session, cause);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {

        Set<Long> keySet = callMap.keySet();
        //将请求队列中的等待结果全部失效
        for (Long key : keySet){
            BasicFuture future = callMap.get(key);
            future.setDone(new ConnectionClosedException("connection to " +session.getRemoteAddress() + " closed"),null);
            //将这个future移除
            callMap.remove(key);
        }
        closed = true;
    }

    public ConcurrentHashMap<Long, BasicFuture> getCallMap() {
        return callMap;
    }
}
