package rpc;

/**
 * RPCException表示在rpc调用过程中发生了IO错误或者其他没有预料到的错误.
 * RPC接口的每个方法必须声明抛出这个exception.
 *
 * 这个Exception实际对应于{@link client.CallException}.
 * Created by zhaoshq on 2017/6/22.
 */
public class RPCException extends Exception {

    private static final long serialVersionUID = -1468522906934192231L;

    public RPCException(String message) {
        super(message);
    }

    public RPCException(Throwable cause) {
        super(cause);
    }

    public RPCException(String message, Throwable cause) {
        super(message, cause);
    }

}
