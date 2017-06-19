package server;

import commons.DataPack;
import odis.serialize.lib.StringWritable;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

import java.util.concurrent.Executor;

/**
 * Created by zhaoshiqiang on 2017/6/6.
 */
public class ServerHandler extends IoHandlerAdapter {

    private Executor executor;
    private IRequestHandler requestHandler;
    private ContextManager contextManager;

    public ServerHandler(Executor executor, IRequestHandler requestHandler, ContextManager contextManager) {
        this.executor = executor;
        this.requestHandler = requestHandler;
        this.contextManager = contextManager;
    }

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
        long seqId = pack.getSeq();
        if (seqId != -1){
            //将session与context关联起来
            StringWritable keyWritable = (StringWritable) pack.getFirst();
            String key = keyWritable.get();
            Context context = null;
            if (key != null){
                context = contextManager.attachSession(key,session);
            }else {
                context = contextManager.attachSession(null,session);
            }
            DataPack respPack = new DataPack();
            respPack.setSeq(seqId);
            respPack.add(new StringWritable(context.getName()));
            session.write(respPack);

        }else {
            //调用方法
            Context context = contextManager.getContext(session);
            executor.execute(new RequestTask(session,pack,requestHandler,context));
        }
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        System.out.println("IDLE" + session.getIdleCount(status));
    }
}
