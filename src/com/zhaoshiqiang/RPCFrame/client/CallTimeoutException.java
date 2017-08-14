package com.zhaoshiqiang.RPCFrame.client;

/**
 * Created by zhaoshq on 2017/6/22.
 */
public class CallTimeoutException extends CallException {
    public CallTimeoutException(String message) {
        super(message);
    }

    public CallTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public CallTimeoutException(Throwable cause) {
        super(cause);
    }
}
