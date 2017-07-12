package client;

import org.apache.mina.common.IdleStatus;

/**
 * Created by zhaoshiqiang on 2017/7/12.
 */
public class HandlerListener implements IHandlerListener {

    private final Client client;

    public HandlerListener(Client client) {
        this.client = client;
    }

    @Override
    public void sessionCreated() throws Exception {
        client.sessionCreated();
    }

    @Override
    public void sessionOpened() throws Exception {
        client.sessionOpened();
    }

    @Override
    public void sessionClosed() throws Exception {
        client.sessionClosed();
    }

    @Override
    public void sessionIdle(IdleStatus var1) throws Exception {
        client.sessionIdle(var1);
    }

    @Override
    public void exceptionCaught(Throwable var1) throws Exception {
        client.exceptionCaught(var1);
    }

    @Override
    public void messageReceived(Throwable var1, Object var2) throws Exception {
        client.messageReceived(var1,var2);
    }

    @Override
    public void messageSent(Object var1) throws Exception {
        client.messageSent(var1);
    }
}
