package demo.hello;

import server.RequestHandler;
import server.Server;

import java.io.IOException;

/**
 * Created by zhaoshiqiang on 2017/6/6.
 */
public class PRCFrameTestServer {
    private static final int port = 8080;

    public static void main(String[] args) throws IOException, InterruptedException {

        Server server =new Server(port,1,new RequestHandler(new HelloImpl()),3);
        server.start();
        System.out.println("HelloServer started on port " + port);
        server.join();

    }
}
