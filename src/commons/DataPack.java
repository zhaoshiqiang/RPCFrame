package commons;

import odis.serialize.IWritable;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by zhaoshiqiang on 2017/6/4.
 */
public class DataPack {

    private long seq = -1;
    private int payloadLength = -1;
    private LinkedList<IWritable> list = new LinkedList<IWritable>();

    public void clear(){
        this.seq = -1;
        this.payloadLength = -1;
        this.list.clear();
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public int getPayloadLength() {
        return payloadLength;
    }

    public void setPayloadLength(int payloadLength) {
        this.payloadLength = payloadLength;
    }

    public List<IWritable> getList() {
        return list;
    }

    public void add(IWritable w){
        list.add(w);
    }

    public IWritable getFirst(){
        return list.getFirst();
    }
}
