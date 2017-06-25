package demo.TestContextDemo;

import odis.serialize.IWritable;
import odis.serialize.lib.IntWritable;
import org.apache.mina.common.IoSession;
import server.Context;
import server.IContextListener;
import server.IRequestHandler;

import java.util.List;

/**
 * Created by zhaoshiqiang on 2017/6/24.
 */
public class ContextDemoRequestHandler implements IRequestHandler, IContextListener {
    @Override
    public IWritable process(List<IWritable> inputList, IoSession session, Context context) throws Throwable {
        IntWritable count = (IntWritable) context.get("_count");
        if (count == null){
            count = new IntWritable(1);
            context.put("_count",count);
        }else {
            count.incAndGet(1);
        }
        return count;
    }

    @Override
    public void onContextCreate(Context context) {
        System.out.println(context.getName() + " create!");
    }

    @Override
    public void onContextDestory(Context context) {
        System.out.println(context.getName() + " destory!");
    }
}
