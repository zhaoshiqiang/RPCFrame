package demo.hello;

import rpc.RPCClient;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

/**
 * Created by zhaoshiqiang on 2017/6/5.
 */
public class RPCFrameTestClient {

    public static void main(String[] args) throws UnknownHostException, ExecutionException, InterruptedException {

        InetSocketAddress addr = new InetSocketAddress("localhost", 8080);
        IHello hello = new RPCClient<IHello>(addr, IHello.class).getProxy();
        Object result = hello.hello("zhaoshiqiang",1);
        System.out.println(result);
        System.out.println(hello.hello());

    }
}
