package client;

/**
 * 请求过程中可能抛出的exception
 * Created by zhaoshq on 2017/6/22.
 */
public class CallException extends Exception {

    private static final long serialVersionUID = -7036269999956272621L;

    public CallException(String message) {
        super(message);
    }

    public CallException(String message, Throwable cause) {
        super(message, cause);
    }

    public CallException(Throwable cause) {
        super(cause);
    }
}
