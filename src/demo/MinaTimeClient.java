package demo;

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

/**
 * Created by zhaoshq on 2017/5/31.
 */
public class MinaTimeClient {

    public static void main(String[] args) throws UnknownHostException {
        SocketConnector connector = new SocketConnector();
        connector.getFilterChain().addLast("logger",new LoggingFilter());
        connector.getFilterChain().addLast("codec",new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
        ConnectFuture connFuture = connector.connect(new InetSocketAddress(InetAddress.getLocalHost(),8080),new TimeClientHandler());
        connFuture.join();
        IoSession session = connFuture.getSession();
        session.write("hi server");
        session.write("quite");
        session.close();

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
