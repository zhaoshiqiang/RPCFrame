package com.zhaoshiqiang.RPCFrame.rpc;

import com.zhaoshiqiang.RPCFrame.client.CallTimeoutException;

/**
 * 当RPC调用timeout的时候抛出这个exception，对应于{@link CallTimeoutException}.
 * Created by zhaoshq on 2017/6/22.
 */
public class RPCTimeoutException extends RPCException {

    private static final long serialVersionUID = -1468522901934892231L;


    public RPCTimeoutException(String message) {
        super(message);
    }

    public RPCTimeoutException(Throwable cause) {
        super(cause);
    }

    public RPCTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
