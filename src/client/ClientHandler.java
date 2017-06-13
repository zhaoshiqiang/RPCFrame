package client;

import commons.DataPack;
import odis.serialize.lib.ObjectWritable;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

import java.util.Map;

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
        DataPack pack = (DataPack) message;
        BasicFuture future = callMap.get(pack.getSeq());
        future.setDone(null,pack.getFirst());
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        super.exceptionCaught(session, cause);
    }
}
