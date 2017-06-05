package demo;

import client.Client;
import odis.serialize.lib.StringWritable;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.SocketConnector;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by zhaoshq on 2017/5/31.
 */
public class MinaTimeClient {

    public static void main(String[] args) throws UnknownHostException, ExecutionException, InterruptedException {
//        Client client = Client.getNewInstance(new InetSocketAddress(InetAddress.getLocalHost(),8080));
//        long start = System.currentTimeMillis();
//        Future future = client.submit("hi zhaoshiqiang");
//        Thread.sleep(50);
//        System.out.println(future.get());
//        long mid = System.currentTimeMillis();
//        System.out.println(mid - start + "ms");


    }

    public static class TimeClientHandler extends IoHandlerAdapter {

        public void messageReceived(IoSession session, Object message) throws Exception {
            String content = message.toString();
            System.out.println("client receive a message is : " + content);
        }

        public void messageSent(IoSession session, Object message) throws Exception {
            System.out.println("messageSent -> ：" + message);
        }

    }
}
