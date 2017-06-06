package server;

import commons.DataPack;
import demo.hello.HelloImpl;
import odis.serialize.IWritable;
import odis.serialize.lib.ObjectWritable;
import odis.serialize.lib.StringWritable;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zhaoshiqiang on 2017/6/6.
 */
public class ServerHandler extends IoHandlerAdapter {
    @Override
    public void sessionCreated(IoSession session) throws Exception {
        System.out.println(session.getRemoteAddress().toString());
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        DataPack pack = (DataPack) message;
        List<IWritable> list = pack.getList();
        Iterator<IWritable> it = list.iterator();
        String methodName = ((StringWritable)it.next()).get();
        System.out.println(methodName);

        Object[] params = new Object[list.size() - 1];
        Class<?>[] paramTypes = new Class<?>[list.size() - 1];
        for (int i=0 ; i<params.length ; i++){
            IWritable obj = it.next();

            params[i] = ((ObjectWritable) obj).getObject();
            paramTypes[i] = ((ObjectWritable) obj).getDeclaredClass();
        }
        HelloImpl instance = new HelloImpl();
        Method m = instance.getClass().getMethod(methodName,paramTypes);
        Object result = m.invoke(instance,params);
        ObjectWritable resultWritable = new ObjectWritable(m.getReturnType(),result);
        DataPack respPack = new DataPack();
        respPack.setSeq(pack.getSeq());
        respPack.add(resultWritable);
        session.write(respPack);

    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        System.out.println("IDLE" + session.getIdleCount(status));
    }
}
