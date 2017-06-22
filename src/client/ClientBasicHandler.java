package client;

import commons.DataPack;
import odis.serialize.IWritable;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import toolbox.misc.LogFormatter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by zhaoshq on 2017/6/1.
 */
public class ClientBasicHandler extends IoHandlerAdapter {

    public static final Logger LOG = LogFormatter.getLogger(Connection.class);
    private final ConcurrentHashMap<Long, BasicFuture> callMap;
    public ClientBasicHandler(ConcurrentHashMap<Long, BasicFuture> callMap) {
        this.callMap = callMap;
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

        future.setDone(null,pack.getFirst());
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        super.exceptionCaught(session, cause);
    }

    public ConcurrentHashMap<Long, BasicFuture> getCallMap() {
        return callMap;
    }
}
