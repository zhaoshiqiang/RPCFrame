package com.zhaoshiqiang.RPCFrame.rpc;

import com.zhaoshiqiang.RPCFrame.client.*;
import odis.serialize.IWritable;
import odis.serialize.lib.ObjectWritable;
import odis.serialize.lib.StringWritable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * RPC客户端，在{@link Client} 基础上实现了rpc调用的接口.
 *
 * rpc客户端的使用方式有两种，一种是同步的rpc调用，例如如下的代码：
 * <code>
 *   ...
 *   RPCClient com.zhaoshiqiang.client = new RPCClient(addr);
 *   com.zhaoshiqiang.client.open();
 *   IProto proto = com.zhaoshiqiang.client.getProxy(IProto.class);
 *   proto.rpcMethod(...);
 *   ...
 * </code>
 *
 * 另外一种办法是异步调用，使用{@link #invoke(Method, Object[])}
 * 方法，例如如下代码：
 * <code>
 *   ...
 *   RPCClient com.zhaoshiqiang.client = new RPCClient(addr);
 *   com.zhaoshiqiang.client.open();
 *   Method method = IProto.class.getMethod(methodName, argTypes);
 *   IFuture future = com.zhaoshiqiang.client.invoke(method, args);
 *   ...
 *   try {
 *       Object result = future.get(timeout, timeoutUnit);
 *       // process result
 *       ...
 *   } catch(ExecutionException e) {
 *       Throwable t = e.getCause();
 *       // process exception
 *       ...
 *   } catch(TimeoutException e) {
 *       // process timeout
 *       ...
 *   }
 * </code>
 * Created by zhaoshiqiang on 2017/6/1.
 */
public class RPCClient {

    protected InetSocketAddress addr;
    protected Client client;
    protected long callTimeout = 0;
    protected TimeUnit callTimeUnit;

    /**
     * 设置请求的timeout，这个设置仅仅在通过 proxy同步调用远程方法的时候生效，对于异步调用
     * 没有作用.
     * @param callTimeout
     * @param unit
     */
    public void setCallTimeout(long callTimeout, TimeUnit unit) {
        this.callTimeout = callTimeout;
        this.callTimeUnit = unit;
    }

    public RPCClient(InetSocketAddress addr) throws CallException {
        this.addr = addr;
        this.client = Client.getNewInstance(addr,RPCInvokeFuture.RPCInvokeFutureFactory.instance);
    }

    public  <T> T getProxy(Class<T> cls) throws CallException{
        client.open();
        validateRPCInterface(cls);
        return (T) Proxy.newProxyInstance(cls.getClassLoader(),new Class[]{cls,IRPCClientCommon.class},new invokeProxy());
    }

    public RPCClient(InetSocketAddress addr, int connectionCount){
        this.addr = addr;
        this.client = Client.getNewInstance(addr,RPCInvokeFuture.RPCInvokeFutureFactory.instance,connectionCount);
    }
    /**
     * 返回连接是否已经被打开.
     * @return
     */
    public boolean isOpened() {
        return client.isOpened();
    }

    /**
     * 关闭连接.
     */
    public void close() throws CallException {
        client.close();
    }

    /**
     * 打开连接
     * @throws CallException
     */
    public void open() throws CallException {
        client.open();
    }
    /**
     * 对于proxy对象的方法调用，这是一个同步的方法，这里可以对函数进行拦截等操作
     * @param proxy
     * @param method
     * @param args
     * @return
     */
    protected Object invokeProxyMethod(Object proxy,Method method, Object[] args) throws Throwable {

        //检查是不是调用RPCClient内部的关闭方法
        String methodName = method.getName();
        if (methodName.startsWith("__")){

            if ( method.getName().equals(IRPCClientCommon.CLOSE_METHOD_NAME)){
                close();
                return null;
            }else {
                throw new RuntimeException("bad internal method : " + methodName);
            }
        }

        Future future = invoke(method,args);

        if (future == null) {
            // cannot submit the request now
            throw new RPCInvalidStateException("com.zhaoshiqiang.client connection state is invalid");
        }

        try {
            if (callTimeout > 0){
                return future.get(callTimeout,callTimeUnit);
            }else {
                return future.get();
            }
        }catch (InterruptedException e) {
            throw new RPCException("call interrupted for method \"" +
                    method.getName() + " \" with args: " + Arrays.deepToString(args), e);
        } catch (ExecutionException e) {
            throw new RPCException(e);
        } catch (TimeoutException e) {
            throw new RPCTimeoutException("call timeout for method \"" +
                    method.getName() + "\" with args : " +
                    Arrays.deepToString(args));
        }
    }

    /**
     * 用于异步调用，把请求发送到rpc服务器，返回future对象. 调用者通过这个future对象检查请求的状态，并且
     * 在请求完成的时候得到返回值.
     * @param method
     * @param args
     * @return
     */
    public Future invoke(Method method, Object[] args) throws ConnectionClosedException {
        return invoke(null,method,args);
    }

    /**
     * 用于异步调用，把请求发送到rpc服务器，返回future对象. 调用者通过这个future对象检查请求的状态，并且
     * 在请求完成的时候得到返回值.
     *
     * @param listener 完成请求的回调接口
     * @param method
     * @param args
     * @return
     */
    public Future invoke(ICallFinishListener listener, Method method, Object[] args) throws ConnectionClosedException {
        int argcount = args==null ? 0 : args.length;
        IWritable[] params = new IWritable[argcount+1];
        params[0]=new StringWritable(method.getName());
        Class<?>[] argTypes = method.getParameterTypes();
        for (int i=0; i<argcount ; i++){
            params[i+1] = new ObjectWritable(argTypes[i],args[i]);
        }
        return client.submit(listener,params);
    }

    protected static void validateRPCInterface(Class<?> protocol) throws ClassCastException{
        Method[] ms = protocol.getMethods();
        for (int i=0; i<ms.length; i++){
            Method m = ms[i];
            Class<?>[] exs = m.getExceptionTypes();
            boolean valid = false;
            for (int j=0; j<exs.length; j++){
                if (exs[j].isAssignableFrom(RPCException.class)){
                    valid = true;
                    break;
                }
            }

            if (!valid){
                StringBuffer exsBuf = new StringBuffer();
                exsBuf.append("<");
                for (Class<?> c : exs){
                    exsBuf.append(c.getName()).append(",");
                }
                exsBuf.append(">");
                throw new ClassCastException("Method" + m.getName()
                + "(...) of RPC interface" + protocol
                + " is not declared as throwing PRCExecption( available exception is " +
                        exsBuf.toString() +"). Plesse fix that.");
            }
        }
    }

    private class invokeProxy implements InvocationHandler{
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            return invokeProxyMethod(proxy,method, args);
        }
    }

    private static class RPCInvokeFuture extends CallFuture {

        public RPCInvokeFuture(ICallFinishListener listener) {
            super(listener);
        }

        @Override
        public Object get() throws ExecutionException, InterruptedException {
            Object o = super.get();
            if (o == null){
                return null;
            }else if (o instanceof ObjectWritable){
                return ((ObjectWritable) o).getObject();
            }else {
                return o;
            }
        }

        @Override
        public Object get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
            Object o = super.get(timeout,unit);
            if (o == null){
                return null;
            }else if (o instanceof ObjectWritable){
                return ((ObjectWritable) o).getObject();
            }else {
                return o;
            }
        }

        public enum  RPCInvokeFutureFactory implements ICallFutureFactory{
            instance;
            @Override
            public BasicFuture create(ICallFinishListener listener) {
                return new RPCInvokeFuture(listener);
            }
        }
    }

    /**
     * 在通过{@link #getProxy(Class)}生成rpc proxy对象的时候，
     * 每个rpc proxy对象实际还实现了{@link IRPCClientCommon}的接口.
     * 这样便可以通过rpc proxy对象释放底层的连接。
     * 使用方式为((RPCClient.IRPCClientCommon)proxyInstance).__close();
     */
    public interface IRPCClientCommon{
        String CLOSE_METHOD_NAME = "__close";
        void __close();
    }
}
