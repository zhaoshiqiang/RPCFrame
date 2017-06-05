package commons;

import org.apache.mina.common.ByteBuffer;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by zhaoshiqiang on 2017/6/5.
 */
public class ByteBufferInput implements DataInput {

    private ByteBuffer buf;

    public ByteBufferInput(ByteBuffer buf) {
        this.buf = buf;
    }

    public ByteBuffer getBuf() {
        return buf;
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        buf.get(b);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        buf.get(b, off, len);
    }

    @Override
    public int skipBytes(int n) throws IOException {
        int pos = buf.position();
        int newPos = buf.position() + n;
        if (newPos > buf.limit())
            newPos = buf.limit();
        buf.position(newPos);
        return newPos-pos;
    }

    @Override
    public boolean readBoolean() throws IOException {
        return buf.get()>0;
    }

    @Override
    public byte readByte() throws IOException {
        return buf.get();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return buf.get() & 0xff;
    }

    @Override
    public short readShort() throws IOException {
        return buf.getShort();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return buf.getShort() & 0xffff;
    }

    @Override
    public char readChar() throws IOException {
        return buf.getChar();
    }

    @Override
    public int readInt() throws IOException {
        return buf.getInt();
    }

    @Override
    public long readLong() throws IOException {
        return buf.getLong();
    }

    @Override
    public float readFloat() throws IOException {
        return buf.getFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return buf.getDouble();
    }

    @Override
    public String readLine() throws IOException {
        throw  new AbstractMethodError("not implemented");
    }

    @Override
    public String readUTF() throws IOException {
        return DataInputStream.readUTF(this);
    }

    public String readAcsiiString() throws IOException {
        int len = readUnsignedShort();
        char[] chars = new char[len];
        for (int i=0; i<len ; i++){
            chars[i] = (char) readByte();
        }
        return new String(chars);
    }
}

