package server;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

import java.util.concurrent.Executor;

/**
 * Created by zhaoshiqiang on 2017/6/6.
 */
public class ServerHandler extends IoHandlerAdapter {

    private Executor executor;
    private IRequestHandler requestHandler;

    public ServerHandler(Executor executor, IRequestHandler requestHandler) {
        this.executor = executor;
        this.requestHandler = requestHandler;
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        System.out.println(session.getRemoteAddress().toString());
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {

        executor.execute(new TreadTask(session,message,requestHandler));
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        System.out.println("IDLE" + session.getIdleCount(status));
    }
}
