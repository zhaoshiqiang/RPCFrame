package com.zhaoshiqiang.RPCFrame.demo.hello;

import com.zhaoshiqiang.RPCFrame.rpc.RPCException;

/**
 * Created by zhaoshiqiang on 2017/6/5.
 */
public interface IHello{
    /**
     *
     * @param name
     * @return "Hello ${name}!".
     * @throws RPCException
     */
    String hello(String name) throws RPCException;

    /**
     *  不带名字的hello接口，应该返回"Hello ${name}!"，其中name是上次hello的时候记住的.
     * @return
     * @throws RPCException
     */
    String hello() throws RPCException;
}
