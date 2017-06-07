package server;

import commons.DataPack;
import odis.serialize.IWritable;
import org.apache.mina.common.IoSession;

/**
 * Created by zhaoshiqiang on 2017/6/7.
 */
public class TreadTask implements Runnable{

    private IoSession session;
    private Object message;
    private IRequestHandler requestHandler;

    public TreadTask(IoSession session, Object message, IRequestHandler requestHandler) {
        this.session = session;
        this.message = message;
        this.requestHandler = requestHandler;
    }

    @Override
    public void run() {
        DataPack pack = (DataPack) message;
        IWritable resultWritable = null;

        try {
            resultWritable = requestHandler.process(pack.getList(),session);
        }  catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        DataPack respPack = new DataPack();
        respPack.setSeq(pack.getSeq());
        respPack.add(resultWritable);
        session.write(respPack);
    }
}
