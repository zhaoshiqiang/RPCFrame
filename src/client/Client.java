package client;

import odis.serialize.IWritable;

import java.util.concurrent.Future;

/**
 * Created by zhaoshiqiang on 2017/6/1.
 */
public class Client {

    private Connection connection;
//    private final Map<Long, Future> callMap = new ConcurrentHashMap<Long, Future>();
    private ClientHandler clientHandler;

    public Future submit(IWritable... objs){
            connection.getSession().write(objs);
        return clientHandler.getFuture();
    }
}
