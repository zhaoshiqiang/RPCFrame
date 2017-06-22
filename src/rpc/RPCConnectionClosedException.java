package rpc;

/**
 * 当下层的连接处于关闭状态下请求失败抛出的exception，对应于 {@link client.ConnectionClosedException}.
 * Created by zhaoshq on 2017/6/22.
 */
public class RPCConnectionClosedException extends RPCException {

    private static final long serialVersionUID = -1468522906934892231L;


    public RPCConnectionClosedException(String message) {
        super(message);
    }

    public RPCConnectionClosedException(Throwable cause) {
        super(cause);
    }

    public RPCConnectionClosedException(String message, Throwable cause) {
        super(message, cause);
    }
}
