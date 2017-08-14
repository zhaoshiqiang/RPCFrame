package com.zhaoshiqiang.RPCFrame.commons;

import odis.serialize.IWritable;

import java.util.LinkedList;
import java.util.List;

/**
 * 中间数据传输中使用的数据结构，保存了请求的sequence number以及实际传输的writable对象列表.
 * Created by zhaoshiqiang on 2017/6/4.
 */
public class DataPack {

    private long seq = -1;
    //传输数据的有效长度
    private int payloadLength = -1;
    //传递的数据，是一个writable对象的list.这里的list要用链表，因为请求的参数个数不定，而且只是按照顺序读取
    private LinkedList<IWritable> list = new LinkedList<IWritable>();

    /**
     * 清空Writable对象列表
     */
    public void clear(){
        this.seq = -1;
        this.payloadLength = -1;
        this.list.clear();
    }

    /**
     * 返回请求的序列号
     * @return
     */
    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    /**
     * 返回请求的数据长度
     * @return
     */
    public int getPayloadLength() {
        return payloadLength;
    }

    public void setPayloadLength(int payloadLength) {
        this.payloadLength = payloadLength;
    }

    /**
     * 返回数据列表
     * @return
     */
    public List<IWritable> getList() {
        return list;
    }

    public void add(IWritable w){
        list.add(w);
    }

    /**
     * 返回数据列表的第一项，在request中的第一项是请求的方法，response的第一项是对应方法调用的结果
     * @return
     */
    public IWritable getFirst(){
        return list.getFirst();
    }
}
