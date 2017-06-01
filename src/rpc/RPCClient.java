package rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by zhaoshiqiang on 2017/6/1.
 */
public class RPCClient {
    public <T> T getProxy(Class<T> cls){
        return (T) Proxy.newProxyInstance(cls.getClassLoader(),new Class[]{cls},new invoke());
    }

    private class invoke implements InvocationHandler{

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return null;
        }
    }
}
