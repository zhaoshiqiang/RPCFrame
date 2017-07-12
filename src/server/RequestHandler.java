package server;

import demo.hello.HelloImpl;
import odis.serialize.IWritable;
import odis.serialize.lib.ObjectWritable;
import odis.serialize.lib.StringWritable;
import org.apache.mina.common.IoSession;
import server.IRequestHandler;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zhaoshiqiang on 2017/6/7.
 */
public class RequestHandler implements IRequestHandler {
    private Object instance;

    public RequestHandler(Object instance) {
        this.instance = instance;
    }

    @Override
    public IWritable process(List<IWritable> inputList, IoSession session,Context context) throws Throwable {
        Iterator<IWritable> it = inputList.iterator();
        String methodName = ((StringWritable)it.next()).get();
//        System.out.println(methodName);

        Object[] params = new Object[inputList.size() - 1];
        Class<?>[] paramTypes = new Class<?>[inputList.size() - 1];
        for (int i=0 ; i<params.length ; i++){
            IWritable obj = it.next();

            params[i] = ((ObjectWritable) obj).getObject();
            paramTypes[i] = ((ObjectWritable) obj).getDeclaredClass();
        }

        Method m = instance.getClass().getMethod(methodName,paramTypes);
        Object result = m.invoke(instance,params);
        return new ObjectWritable(result.getClass(),result);
    }


}
