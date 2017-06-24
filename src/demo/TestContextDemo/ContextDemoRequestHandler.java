package demo.TestContextDemo;

import odis.serialize.IWritable;
import odis.serialize.lib.IntWritable;
import org.apache.mina.common.IoSession;
import server.Context;
import server.IRequestHandler;

import java.util.List;

/**
 * Created by zhaoshiqiang on 2017/6/24.
 */
public class ContextDemoRequestHandler implements IRequestHandler {
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
}
