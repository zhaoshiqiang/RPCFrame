package rpc;

import client.Client;
import odis.serialize.IWritable;
import odis.serialize.lib.ObjectWritable;
import odis.serialize.lib.StringWritable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Future;

/**
 * Created by zhaoshiqiang on 2017/6/1.
 */
public class RPCClient {

    private Client client;

    public <T> T getProxy(Class<T> cls){
        return (T) Proxy.newProxyInstance(cls.getClassLoader(),new Class[]{cls},new invokeProxy());
    }

    private class invokeProxy implements InvocationHandler{

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            int argcount = args==null ? 0 : args.length;
            IWritable[] params = new IWritable[argcount+1];
            params[0]=new StringWritable(method.getName());
            Class<?>[] argTypes = method.getParameterTypes();
            for (int i=0; i<argcount ; i++){
                params[i+1] = new ObjectWritable(argTypes[i],args[i]);
            }
            Future future = client.submit(params);
            return future.get();
        }
    }
}
