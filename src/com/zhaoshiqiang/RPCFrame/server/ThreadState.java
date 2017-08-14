package com.zhaoshiqiang.RPCFrame.server;

import org.apache.mina.common.IoSession;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 线程状态，用于设置和访问当前线程相关的数据，目前可以访问的数据包括context和session.
 * 这个类便于在rpc方法中得到一些没有办法通过接口传入的数据，例如，我们可以在方法中
 * 通过如下方法得到当前的context:
 * <code>
 *   ...
 *   Context context = RPCThreadState.getContext();
 *   ...
 * </code>
 * 实际的实现办法是将状态以当前线程为key保存在一个状态表(hash表)中便于访问.
 *
 * 状态的设置和取消发生在进入/退出{@link IRequestHandler#process(List, IoSession, Context)}方法调用的时候.
 *
 * Created by zhaoshq on 2017/6/20.
 */
public class ThreadState {

    private static final ConcurrentHashMap<Thread,State> currentState = new ConcurrentHashMap<Thread,State>();

    /**
     * 设置当前线程的context和session.
     * @param context
     * @param session
     */
    public static void setState(Context context,IoSession session){
        currentState.put(Thread.currentThread(),new State(context,session));
    }

    /**
     * 获得当前线程使用的context.
     * @return
     */
    public static Context getContext(){

        State st = currentState.get(Thread.currentThread());
        return st == null ? null : st.context;
    }

    public static IoSession getIoSession() {
        State st = currentState.get(Thread.currentThread());
        return st == null ? null : st.session;
    }

    /**
     * 释放当前线程的状态信息.
     */
    public static void removeState() {
        currentState.remove(Thread.currentThread());
    }

    private static class State{
        private Context context;
        private IoSession session;

        public State(Context context, IoSession session) {
            this.context = context;
            this.session = session;
        }

    }
}
