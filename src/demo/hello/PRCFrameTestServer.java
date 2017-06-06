package demo.hello;

import server.Server;

import java.io.IOException;

/**
 * Created by zhaoshiqiang on 2017/6/6.
 */
public class PRCFrameTestServer {
    private static final int port = 8080;

    public static void main(String[] args) throws IOException {

        new Server(port);
        System.out.println("HelloServer started on port " + port);
    }
}
