package com.zhaoshiqiang.RPCFrame.demo.hello;

import com.zhaoshiqiang.RPCFrame.rpc.RPCClient;

import java.net.InetSocketAddress;

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
        IHello hello = new RPCClient(addr).getProxy(IHello.class);
        Object result = hello.hello(args[2]);
        System.out.println(result);
        System.out.println(hello.hello());
    }
}
