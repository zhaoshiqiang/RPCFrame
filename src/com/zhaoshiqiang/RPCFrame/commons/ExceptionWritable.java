package com.zhaoshiqiang.RPCFrame.commons;

import odis.serialize.lib.BytesWritable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Exception的writable容器，用于在服务器和客户端之间传输服务器执行过程中发生的
 * exception，这里要求exception必须是serializable.
 *
 * Created by zhaoshq on 2017/6/22.
 */
public class ExceptionWritable extends BytesWritable {
    private Bos bos = new Bos();

    public void set(Throwable t) {
        try {
            bos.reset();
            new ObjectOutputStream(bos).writeObject(t);
            this.setBuffer(bos.getRawBuf(), bos.getSize());
        } catch(Exception e) {
            throw new RuntimeException("serialize throwable failed", e);
        }
    }

    public Throwable get() {
        ByteArrayInputStream bin = new ByteArrayInputStream(this.data(), 0, this.size());
        try {
            return (Throwable) new ObjectInputStream(bin).readObject();
        } catch(Exception e) {
            throw new RuntimeException("read throwable from buf failed", e);
        }
    }

    private static class Bos extends ByteArrayOutputStream {

        public byte [] getRawBuf() {
            return buf;
        }

        public int getSize() {
            return count;
        }

    }
}
