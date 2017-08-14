package com.zhaoshiqiang.RPCFrame.client;

/**
 * Created by zhaoshq on 2017/6/22.
 */
public class ConnectionClosedException extends CallException {

    public ConnectionClosedException(String message) {
        super(message);
    }

    public ConnectionClosedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectionClosedException(Throwable cause) {
        super(cause);
    }
}
