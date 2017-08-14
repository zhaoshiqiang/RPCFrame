package com.zhaoshiqiang.RPCFrame.demo;

import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

/**
 * Created by zhaoshq on 2017/5/31.
 */
public class MinaTimeClient {

    public static void main(String[] args) throws UnknownHostException, ExecutionException, InterruptedException {
//        Client com.zhaoshiqiang.client = Client.getNewInstance(new InetSocketAddress(InetAddress.getLocalHost(),8080));
//        long start = System.currentTimeMillis();
//        Future future = com.zhaoshiqiang.client.submit("hi zhaoshiqiang");
//        Thread.sleep(50);
//        System.out.println(future.get());
//        long mid = System.currentTimeMillis();
//        System.out.println(mid - start + "ms");


    }

    public static class TimeClientHandler extends IoHandlerAdapter {

        public void messageReceived(IoSession session, Object message) throws Exception {
            String content = message.toString();
            System.out.println("com.zhaoshiqiang.client receive a message is : " + content);
        }

        public void messageSent(IoSession session, Object message) throws Exception {
            System.out.println("messageSent -> ：" + message);
        }

    }
}
