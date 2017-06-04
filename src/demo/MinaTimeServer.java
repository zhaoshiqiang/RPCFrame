package demo;

import org.apache.mina.common.*;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * Created by zhaoshq on 2017/5/31.
 */
public class MinaTimeServer {
    private static final int port = 8080;

    public static void main(String[] args) throws IOException {

        IoAcceptor acceptor = new SocketAcceptor();
        IoAcceptorConfig config = new SocketAcceptorConfig();
        DefaultIoFilterChainBuilder chain = config.getFilterChain();
        chain.addLast("logger",new LoggingFilter());
        chain.addLast("codec",new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
        acceptor.bind(new InetSocketAddress(port),new TimeServerHandler(),config);
        System.out.println("HelloServer started on port " + port);
    }

    static class TimeServerHandler extends IoHandlerAdapter{

        @Override
        public void sessionCreated(IoSession session) throws Exception {
            System.out.println(session.getRemoteAddress().toString());
        }

        @Override
        public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
            cause.printStackTrace();
        }

        @Override
        public void messageReceived(IoSession session, Object message) throws Exception {
            String strMsg = message.toString();
            if (strMsg.trim().equalsIgnoreCase("quit")){
                session.close();
                return;
            }
            Thread.sleep(100);
            session.write("hi client!");
            System.out.println("Message written : " + strMsg);
        }

        @Override
        public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
            System.out.println("IDLE" + session.getIdleCount(status));
        }
    }

}
