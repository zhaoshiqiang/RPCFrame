package demo.hello;

import rpc.RPCClient;
import rpc.RPCException;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by zhaoshiqiang on 2017/6/5.
 */
public class RPCFrameTestClient {

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.out.println("args: hostname port name");
            return;
        }
        InetSocketAddress addr = new InetSocketAddress(args[0], Integer.parseInt(args[1]));
        IHello hello = new RPCClient<IHello>(addr, IHello.class).getProxy();
        Object result = hello.hello(args[2]);
        System.out.println(result);
        System.out.println(hello.hello());

//        RPCClient client = new RPCClient(addr);
//        client.open();
//        Method helloMethod = IHello.class.getMethod("hello", String.class);
//        Future future = client.invoke(helloMethod, new Object[]{"zhaoshiqiang"});
//        future.get();
    }
}
