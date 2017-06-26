package demo.TestContextDemo;

import client.Client;
import odis.serialize.IWritable;
import odis.serialize.lib.IntWritable;
import server.Server;

import java.net.InetSocketAddress;
import java.util.concurrent.Future;

/**
 * 用TestContext测试如下的内容：
 * <ul>
 * <li>同一个client的多连接共享context
 * <li>不同client使用不同的context
 * <li>context的创建和释放
 * </ul>
 * Created by zhaoshiqiang on 2017/6/24.
 */
public class ContextTest {

    public static void main(String[] args) throws Exception {
        Server server = new Server(8080,new ContextDemoRequestHandler());

        InetSocketAddress addr = new InetSocketAddress("localhost", 8080);
        Client client1 = Client.getNewInstance(addr,3);
        client1.open();
        Client client2 = Client.getNewInstance(addr, 2);
        client2.open();
        for (int i = 0; i < 100; i++) {
            Future future = client1.submit(null,new IWritable[0]);
            Integer result = ((IntWritable) future.get()).get();
            if ( i+1 != result){
                System.out.println("第" + i+1 + "次 输出的不匹配结果为：" + result);
            }
        }
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println(server.getContextManager().getContextSize());
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~");
        client1.close();
        Thread.sleep(100);
        System.out.println(server.getContextManager().getContextSize());
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~");

        for (int i = 0; i < 200; i++) {
            Future future = client2.submit(null,new IWritable[0]);
            Integer result = ((IntWritable) future.get()).get();
            if ( i+1 != result){
                System.out.println("第" + i+1 + "次 输出的不匹配结果为：" + result);
            }
        }
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~");
        client2.close();
        Thread.sleep(100);
        System.out.println(server.getContextManager().getContextSize());
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~");
    }
}
