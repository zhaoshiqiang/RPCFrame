package server;

import odis.serialize.IWritable;
import org.apache.mina.common.IoSession;

import java.util.List;

/**
 * Created by zhaoshiqiang on 2017/6/7.
 */
public interface IRequestHandler {

    public IWritable process(List<IWritable> inputList, IoSession session) throws Throwable;
}
