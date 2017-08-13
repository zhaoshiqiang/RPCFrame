package com.zhaoshiqiang.RPCFrame.commons;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 这是一个线程工程，线程池通过这个此工厂来创建线程
 * 这里只是为线程指定名字，从而区别不同线程池中的线程
 * Created by zhaoshq on 2017/7/7.
 */
public class NamedThreadFactory implements ThreadFactory {
    private AtomicInteger id = new AtomicInteger(0);
    private final String namePrefix;
    private final boolean deamon;

    public NamedThreadFactory(String namePrefix, boolean deamon) {
        this.namePrefix = namePrefix;
        this.deamon = deamon;
    }

    /**
     * 定制自己的线程，这里只是为线程设置名字和deamon类型，所以只是用现有的{@link Thread}，
     * 还可以扩展{@link Thread}来实现更复杂的功能，如为线程指定名字，维护一些统计信息（包括有多少个线程被创建和销毁）
     * 在线程被创建或者终止时把调试信息写入日志
     * @param r
     * @return 返回线程池中需要的线程
     */
    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r,namePrefix + "_" + id.getAndSet(1));
        thread.setDaemon(deamon);
        return thread;
    }
}
