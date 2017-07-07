package server;

import commons.DataPack;
import commons.ExceptionWritable;
import odis.serialize.IWritable;
import org.apache.mina.common.IoSession;

/**
 * Created by zhaoshiqiang on 2017/6/7.
 */
public class RequestTask implements Runnable{

    private IoSession session;
    private DataPack pack;
    private IRequestHandler requestHandler;
    private Context context;

    public RequestTask(IoSession session, DataPack pack, IRequestHandler requestHandler, Context context) {
        this.session = session;
        this.pack = pack;
        this.requestHandler = requestHandler;
        this.context = context;
    }

    public IoSession getSession() {
        return session;
    }

    public DataPack getPack() {
        return pack;
    }

    @Override
    public void run() {

        IWritable resultWritable = null;

        try {

            ThreadState.setState(context,session);
            resultWritable = requestHandler.process(pack.getList(),session,context);
        }  catch (Throwable throwable) {
            //这里需要把这个异常包裹成IWritable作为结果返回
            ExceptionWritable ew = new ExceptionWritable();
            ew.set(throwable);
            resultWritable = ew;
        }finally {
            ThreadState.removeState();
        }

        DataPack respPack = new DataPack();
        respPack.setSeq(pack.getSeq());
        respPack.add(resultWritable);
        session.write(respPack);
    }
}
