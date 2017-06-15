package server;

import odis.serialize.IWritable;
import org.apache.mina.common.IoSession;

import java.util.List;

/**
 * 任务处理的接口，处理输入的数据（List<IWritable对象），得到输出的数据（IWritable对象）.
 * Created by zhaoshiqiang on 2017/6/7.
 */
public interface IRequestHandler {

    /**
     * 处理输入的请求数据，并且产生输出的数据. 这个方法是会被多个线程同时调用的，所以
     * 实现代码必须保证线程安全.
     * @param inputList
     * @param session
     * @return
     * @throws Throwable
     */
    IWritable process(List<IWritable> inputList, IoSession session) throws Throwable;
}
