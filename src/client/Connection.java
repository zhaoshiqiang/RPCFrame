package client;

import org.apache.mina.common.IoSession;

/**
 * Created by zhaoshq on 2017/6/1.
 */
public class Connection {

    private IoSession session;

    public IoSession getSession() {
        return session;
    }
}
