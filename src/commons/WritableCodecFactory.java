package commons;

import odis.serialize.IWritable;
import odis.serialize.lib.ObjectWritable;
import odis.serialize.lib.StringWritable;
import odis.serialize.lib.UTF8Writable;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.*;
import toolbox.misc.ClassUtils;

import java.util.List;

/**
 * Created by zhaoshiqiang on 2017/6/4.
 */
public class WritableCodecFactory implements ProtocolCodecFactory {

    private static final int LONG_BYTES = Long.SIZE / Byte.SIZE;
    private static final int INT_BYTES = Integer.SIZE / Byte.SIZE;
    private static final int HEADER_LEN = LONG_BYTES + INT_BYTES;

    @Override
    public ProtocolEncoder getEncoder() throws Exception {
        return new WritableProtocolEncoder();
    }

    @Override
    public ProtocolDecoder getDecoder() throws Exception {
        return new WritableProtocolDecoder();
    }

    private class WritableProtocolEncoder implements ProtocolEncoder{

        @Override
        public void encode(IoSession ioSession, Object o, ProtocolEncoderOutput protocolEncoderOutput) throws Exception {
            DataPack encodePack = (DataPack) o;
            ByteBufferOutput output = new ByteBufferOutput(ByteBuffer.allocate(1024).setAutoExpand(true));
            output.writeLong(encodePack.getSeq());
            int oldPos = output.position();
            output.writeInt(0);
            List<IWritable> list = encodePack.getList();
            int listsize = list.size();
            output.writeInt(listsize);
            for (IWritable writable : list){
                output.writeAsciiString(writable.getClass().getName());
                writable.writeFields(output);
            }

            int lastPos = output.position();
            output.position(oldPos);
            output.writeInt(lastPos-HEADER_LEN);
            output.position(lastPos);
            output.flish();
            protocolEncoderOutput.write(output.getBuf());
        }

        @Override
        public void dispose(IoSession ioSession) throws Exception {
        }
    }

    private class WritableProtocolDecoder extends CumulativeProtocolDecoder{


        @Override
        protected boolean doDecode(IoSession ioSession, ByteBuffer byteBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception {
            DataPack decodePack = new DataPack();
            ByteBufferInput input = new ByteBufferInput(byteBuffer);
            decodePack.setSeq(input.readLong());
            int dataSize = input.readInt();
            decodePack.setPayloadLength(dataSize);
            if (byteBuffer.remaining() < dataSize){
                return false;
            }
            int listSize = byteBuffer.getInt();
            for (int i=0; i< listSize ; i++){
                String classname = input.readAcsiiString();

                IWritable writable = (IWritable) ClassUtils.newInstance(Class.forName(classname));
                writable.readFields(input);
                decodePack.add(writable);
            }
            protocolDecoderOutput.write(decodePack);
            return true;
        }

    }
}
