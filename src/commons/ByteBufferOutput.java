package commons;

import org.apache.mina.common.ByteBuffer;

import java.io.DataOutput;
import java.io.IOException;
import java.io.UTFDataFormatException;

/**
 * 在apache mina的ByteBuffer基础上实现的DataOutput接口，用于兼容IWritable对象的输出.
 * 需要注意的是，如下方法没有实现: {@link #writeUTF(String)}, {@link #writeChars(String)},
 * {@link #writeBytes(String)}.
 *
 * 在基本接口以外，还增加了{@link #writeAsciiString(String)}方法加速对于”类名“这样的ascii
 * 字符串的输出.
 * Created by zhaoshiqiang on 2017/6/4.
 */
public class ByteBufferOutput implements DataOutput {

    private ByteBuffer buf;

    public ByteBufferOutput(ByteBuffer buf) {
        this.buf = buf;
    }

    public ByteBuffer getBuf() {
        return buf;
    }

    @Override
    public void write(int b) throws IOException {
        buf.put((byte) b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        buf.put(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        buf.put(b, off, len);
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        buf.put((byte) (v? 1 : 0));
    }

    @Override
    public void writeByte(int v) throws IOException {
        buf.put((byte) v);
    }

    @Override
    public void writeShort(int v) throws IOException {
        buf.putShort((short) v);
    }

    @Override
    public void writeChar(int v) throws IOException {
        buf.putChar((char) v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        buf.putInt(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        buf.putLong(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        buf.putFloat(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        buf.putDouble(v);
    }


    @Override
    public void writeBytes(String s) throws IOException {
        char[] chars = s.toCharArray();
        for (int i=0; i<chars.length ; i++){
            write((byte)chars[i]);
        }
    }

    @Override
    public void writeChars(String s) throws IOException {
        char[] chars = s.toCharArray();
        for (int i=0; i<chars.length ; i++){
            write(chars[i]);
        }
    }
    /**
     * 写入一个完全由Ascii组成的字符串，例如类名。这里避免了字符集的转换
     * @param s
     * @throws IOException
     */
    public void writeAsciiString(String s) throws IOException {
        int len = s.length();
        writeShort(len);
        for (int i=0; i<s.length(); i++){
            writeByte(s.charAt(i));
        }
    }
    @Override
    public void writeUTF(String s) throws IOException {
        writeUTF(s, this);
    }

    private static int writeUTF(String str, ByteBufferOutput out) throws IOException{
        int strlen = str.length();
        int utflen = 0;
        int c, count = 0;

        /* use charAt instead of copying String to char array */
        for (int i = 0; i < strlen; i++) {
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }

        if (utflen > 65535)
            throw new UTFDataFormatException(
                    "encoded string too long: " + utflen + " bytes");

        byte[] bytearr = new byte[utflen+2];

        bytearr[count++] = (byte) ((utflen >>> 8) & 0xFF);
        bytearr[count++] = (byte) ((utflen >>> 0) & 0xFF);

        int i=0;
        for (i=0; i<strlen; i++) {
            c = str.charAt(i);
            if (!((c >= 0x0001) && (c <= 0x007F))) break;
            bytearr[count++] = (byte) c;
        }

        for (;i < strlen; i++){
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                bytearr[count++] = (byte) c;

            } else if (c > 0x07FF) {
                bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytearr[count++] = (byte) (0x80 | ((c >>  6) & 0x3F));
                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
            } else {
                bytearr[count++] = (byte) (0xC0 | ((c >>  6) & 0x1F));
                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
            }
        }
        out.write(bytearr, 0, utflen+2);
        return utflen + 2;
    }

    public int position(){
        return buf.position();
    }

    public DataOutput position(int p){
        buf.position(p);
        return this;
    }

    public DataOutput flish(){
        buf.flip();
        return this;
    }
}
