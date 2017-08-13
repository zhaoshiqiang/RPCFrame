package com.zhaoshiqiang.RPCFrame.server;

import org.apache.mina.common.IoSession;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 这里用HashSet<IoSession>而不用concurrentHashMap是
 * 由于concurrentHashMap并发粒度小的原因，所以size方法返回的并不是concurrentHashMap的准确值。
 * 关于concurrentHashMap还有一点需要注意的是其put方法的value也不能为null。
 * 这个类会被多个线程调用，需要考虑并发
 * Created by zhaoshiqiang on 2017/6/18.
 */
public class Context extends HashMap {
    private final String name;
    private HashSet<IoSession> sessions = new HashSet<IoSession>();
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    public Context(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getSessionCount(){
        lock.readLock().lock();
        try {
            return sessions.size();
        }finally {
            lock.readLock().unlock();
        }
    }

    public void addSession(IoSession session){

        lock.writeLock().lock();
        try {
            sessions.add(session);
        }finally {
            lock.writeLock().unlock();
        }
    }

    public void removeSession(IoSession session){

        lock.writeLock().lock();
        try {
            sessions.remove(session);
        }finally {
            lock.writeLock().unlock();
        }
    }

}
