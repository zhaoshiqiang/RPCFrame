package com.zhaoshiqiang.RPCFrame.client;

import java.util.concurrent.Future;

/**
 * 回调函数接口
 * Created by zhaoshiqiang on 2017/6/13.
 */
public interface ICallFinishListener {
    /**
     * 在call调用done或者cancelled中应该调用这个方法
     * @param call
     */
    void onFinish(Future call);
}
