package server;

import org.apache.mina.common.IoSession;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 这个类会被多个线程调用，需要考虑并发
 * Created by zhaoshiqiang on 2017/6/18.
 */
public class ContextManager {

    private final IContextListener contextListener;

    private static final String CONTEXT_NAME = "__context_";

    private ConcurrentHashMap<String,Context> contexts ;

    private AtomicLong contextIdGenerator = new AtomicLong(0);

    public ContextManager(IContextListener contextListener) {
        this.contextListener = contextListener;
        this.contexts = new ConcurrentHashMap<String,Context>();
    }
    public ContextManager(){
        contextListener = null;
        this.contexts = new ConcurrentHashMap<String,Context>();
    }
    public Context attachSession(String key, IoSession session){
        Context oldContext = contexts.get(key);
        if (oldContext != null){
            detachSession(session);
        }
        Context context = null;
            if (key == null){
                key = createContextKey(session);
            }else {
                context = contexts.get(key);
            }
            if (context == null){
                context = new Context(key);
                contexts.put(key,context);
            }
            //将context和session关联起来
            context.addSession(session);
            session.setAttribute(CONTEXT_NAME,context);
        if (contextListener != null){
            contextListener.onContextCreate(context);
        }
        return context;
    }

    public boolean detachSession(IoSession session){
        boolean remove = false;
        Context context = (Context) session.getAttribute(CONTEXT_NAME);

        //将context和session关系解除
        context.removeSession(session);
        session.removeAttribute(CONTEXT_NAME);
        //如果已经没有session关联context，就从contexts中删除该context
        if (context.getSessionCount() == 0){
            contexts.remove(context.getName());

            if (contextListener != null){
                contextListener.onContextDestory(context);
            }
            remove = true;
        }
        return remove;
    }

    public Context getContext(IoSession session){
        return (Context) session.getAttribute(CONTEXT_NAME);
    }

    private String createContextKey(IoSession session){
        InetSocketAddress addr = (InetSocketAddress) session.getRemoteAddress();
        return addr.toString()+contextIdGenerator.addAndGet(1);
    }

}
