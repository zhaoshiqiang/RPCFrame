package client;

/**
 * 这个类要被多线程调用，需要考虑并发
 * Created by zhaoshiqiang on 2017/7/12.
 */
public class HandlerListener implements IHandlerListener {

    private final Client client;

    public HandlerListener(Client client) {
        this.client = client;
    }

    @Override
    public void sessionCreated() throws Exception {

    }

    @Override
    public void sessionOpened() throws Exception {

    }

    @Override
    public void sessionClosed() throws Exception {
        //client要操作ConnectionsManager做一些操作，比如从ConnectionsManager中移除关闭的connection或者重新尝试打开connection
        client.close();
    }


    @Override
    public void exceptionCaught(Throwable cause) throws Exception {
        client.onConnectionException(cause);

    }

    @Override
    public void messageReceived(Throwable cause, Object o) throws Exception {

    }

    @Override
    public void messageSent(Object o) throws Exception {

    }
}
