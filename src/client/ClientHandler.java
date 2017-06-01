package client;

import commons.BasicFuture;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

import java.util.concurrent.Future;

/**
 * Created by zhaoshq on 2017/6/1.
 */
public class ClientHandler extends IoHandlerAdapter {

    private BasicFuture future;

    public BasicFuture getFuture() {
        return future;
    }

    public void setFuture(BasicFuture future) {
        this.future = future;
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        future.setDone(null,message);
    }
}
