package client;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoSession;

/**
 * Created by zhaoshiqiang on 2017/7/12.
 */
public interface IHandlerListener {

    void sessionCreated() throws Exception;

    void sessionOpened() throws Exception;

    void sessionClosed() throws Exception;


    void exceptionCaught(Throwable var1) throws Exception;

    void messageReceived(Throwable var1, Object var2) throws Exception;

    void messageSent(Object var1) throws Exception;
}
