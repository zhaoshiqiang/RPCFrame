package server;

/**
 * Created by zhaoshiqiang on 2017/6/15.
 */
public interface IRequestHandlerFactory {
    IRequestHandler create(Object instance);
}
