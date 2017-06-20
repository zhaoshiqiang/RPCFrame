package demo.hello;

import server.Context;
import server.ThreadState;

/**
 * Created by zhaoshiqiang on 2017/6/5.
 */
public class HelloImpl implements IHello {
    @Override
    public String hello(String name, int number) {
        Context context = ThreadState.getContext();
        if (context != null){
            context.put("name",name);
        }
        return "hello " + name + " ! number is " + number;
    }

    @Override
    public String hello() {
        Context context = ThreadState.getContext();
        if (context != null){
            return "hello" + context.get("name");
        }else {
            return null;
        }
    }

}
