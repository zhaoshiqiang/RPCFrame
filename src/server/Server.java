package server;

import commons.WritableCodecFactory;
import demo.MinaTimeServer;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoAcceptorConfig;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by zhaoshiqiang on 2017/6/6.
 */
public class Server {

    public Server(int port) throws IOException {
        IoAcceptor acceptor = new SocketAcceptor();
        IoAcceptorConfig config = new SocketAcceptorConfig();
        DefaultIoFilterChainBuilder chain = config.getFilterChain();
        chain.addLast("logger",new LoggingFilter());
        chain.addLast("codec",new ProtocolCodecFilter(new WritableCodecFactory()));
        acceptor.bind(new InetSocketAddress(port), new ServerHandler(),config);
    }
}
