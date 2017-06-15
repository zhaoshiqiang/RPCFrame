package server;

import commons.DataPack;
import odis.serialize.IWritable;
import org.apache.mina.common.IoSession;

/**
 * Created by zhaoshiqiang on 2017/6/7.
 */
public class RequestTask implements Runnable{

    private IoSession session;
    private DataPack pack;
    private IRequestHandler requestHandler;

    public RequestTask(IoSession session, DataPack pack, IRequestHandler requestHandler) {
        this.session = session;
        this.pack = pack;
        this.requestHandler = requestHandler;
    }

    @Override
    public void run() {

        IWritable resultWritable = null;

        try {
            resultWritable = requestHandler.process(pack.getList(),session);
        }  catch (Throwable throwable) {
            //这里需要把这个异常包裹成IWritable作为结果返回
            throwable.printStackTrace();
        }

        DataPack respPack = new DataPack();
        respPack.setSeq(pack.getSeq());
        respPack.add(resultWritable);
        session.write(respPack);
    }
}
