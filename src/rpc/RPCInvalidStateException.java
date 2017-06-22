package rpc;

/**
 * Created by zhaoshq on 2017/6/22.
 */
public class RPCInvalidStateException extends RPCException {

    private static final long serialVersionUID = -1468522901984892231L;

    public RPCInvalidStateException(String message) {
        super(message);
    }

    public RPCInvalidStateException(Throwable cause) {
        super(cause);
    }

    public RPCInvalidStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
