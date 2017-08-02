package demo.hello;

import server.RequestHandler;
import server.Server;

import java.io.IOException;

/**
 * Created by zhaoshiqiang on 2017/6/6.
 */
public class PRCFrameTestServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 1) {
            System.out.println("args: port");
            return;
        }
        int port = Integer.parseInt(args[0]);
        Server server =new Server(port,new HelloImpl());
        server.start();
        System.out.println("HelloServer started on port " + port);
        server.join();

    }
}
