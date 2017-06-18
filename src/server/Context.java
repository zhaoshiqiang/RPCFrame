package server;

import org.apache.mina.common.IoSession;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 这个类会被多个线程调用，需要考虑并发
 * Created by zhaoshiqiang on 2017/6/18.
 */
public class Context extends HashMap {
    private final String name;
    private ConcurrentHashMap<IoSession,Object> sessions = new ConcurrentHashMap<IoSession, Object>();

    public Context(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getSessionCount(){
        return sessions.size();
    }

    public void addSession(IoSession session){
        sessions.put(session,null);
    }

    public void removeSession(IoSession session){
        sessions.remove(session);
    }

}
