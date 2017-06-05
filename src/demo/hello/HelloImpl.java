package demo.hello;

/**
 * Created by zhaoshiqiang on 2017/6/5.
 */
public class HelloImpl implements IHello {
    @Override
    public String hello(String name, int number) {
        return "hello " + name + " ! number is " + number;
    }
}
