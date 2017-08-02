package demo.hello;

import server.Context;
import server.IContextListener;
import server.ThreadState;

/**
 * Created by zhaoshiqiang on 2017/6/5.
 */
public class HelloImpl implements IHello , IContextListener{
    @Override
    public String hello(String name) {
        Context context = ThreadState.getContext();
        if (context != null){
            context.put("name",name);
        }
        return "hello " + name +" !";
    }

    @Override
    public String hello() {
        Context context = ThreadState.getContext();
        if (context != null){
            return "hello" + context.get("name");
        }else {
            return "Hello, may I known your name?";
        }
    }

    @Override
    public void onContextCreate(Context context) {
        context.put("_start", System.currentTimeMillis());
    }

    @Override
    public void onContextDestory(Context context) {
        long start = (Long) context.get("_start");
         long end = System.currentTimeMillis();
         String name = (String) context.get("name");
         if (name == null) {
             System.out.println("unknown user stayed here for " + (end-start) + "ms.");
         } else {
             System.out.println(name + " stayed here for " + (end-start) + "ms.");
         }
    }
}
