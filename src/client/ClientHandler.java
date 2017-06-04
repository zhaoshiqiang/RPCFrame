package client;

import commons.BasicFuture;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * Created by zhaoshq on 2017/6/1.
 */
public class ClientHandler extends IoHandlerAdapter {

    private final Map<Long, BasicFuture> callMap;

    public ClientHandler(Map<Long, BasicFuture> callMap) {
        this.callMap = callMap;
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        BasicFuture future = callMap.get(1l);
        future.setDone(null,message);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        super.exceptionCaught(session, cause);
    }
}
