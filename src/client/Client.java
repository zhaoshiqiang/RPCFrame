package client;

import commons.BasicFuture;
import odis.serialize.IWritable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zhaoshiqiang on 2017/6/1.
 */
public class Client {

    private Connection connection;
    private final Map<Long, BasicFuture> callMap = new ConcurrentHashMap<Long, BasicFuture>();

    private AtomicLong reqId = new AtomicLong(0);
    public Future submit(IWritable... objs){
        long id = reqId.addAndGet(1);
        BasicFuture future = new BasicFuture();
        callMap.put(id,future);
        connection.getSession().write(objs);
        return future;
    }
}
