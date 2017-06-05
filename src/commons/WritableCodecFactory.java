package commons;

import odis.serialize.IWritable;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.*;
import toolbox.misc.ClassUtils;

import java.util.List;

/**
 * 按照数据传输协议进行数据的encode和decode.
 *  <strong>传输协议</strong>
 * 客户端和服务器之间的数据传输协议如下:
 * REQUEST = REQ_SEQ(long) REQ_LEN(int) REQ_DATA
 * (RESPONSE = REQ_SEQ(long) RES_LEN(int) RES_DATA)
 * REQ_DATA = OBJ_COUNT(int) OBJ*
 *
 * 其中各个域定义如下:
 * <ul>
 * <li>REQ_SEQ : 请求的序列号，一个用于区分不同的请求的long(64bits). 所有的序列号都是大于等于0的，
 * <li>REQ_LEN : 请求的数据长度，也就是REQ_DATA的byte数，是一个int(32bits).
 * <li>REQ_DATA : 请求的数据，是一个writable对象的list.其结构为: list大小+(writable.classname + item)+...
 * </ul>
 *
 * <strong>字节序</strong><p>
 * 在对象序列化过程中，虽然直接使用了IWritable，但是由于性能问题，这里采用的传输的字节序目前是java默认的字节序(
 * 高位在前)，这个和odis里面DataInputBuffer/DataOutputBuffer使用的字节序是不同的.
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
            //先将结果转换成DataPack
            DataPack encodePack = (DataPack) o;
            ByteBufferOutput output = new ByteBufferOutput(ByteBuffer.allocate(1024).setAutoExpand(true));
            //写入请求序列号
            output.writeLong(encodePack.getSeq());
            //记录在buff中写入请求数据有效长度的位置
            int oldPos = output.position();
            //先随便写一个int数据占位置
            output.writeInt(0);
            List<IWritable> list = encodePack.getList();
            int listsize = list.size();
            //写入传输数据列表的大小
            output.writeInt(listsize);
            //将传输数据列表中的每一项item写入list
            for (IWritable writable : list){
                //写入item对应writable的class名称，方便在解码时构造出对应的writable
                output.writeAsciiString(writable.getClass().getName());
                //写入item本身
                writable.writeFields(output);
            }
            //buf中数据的总大小
            int lastPos = output.position();
            //准备写入传输数据的有效长度
            output.position(oldPos);
            output.writeInt(lastPos-HEADER_LEN);
            output.position(lastPos);
            //做好读取buf数据准备，limit移动到pos位置，pos，mark还原为初值
            output.flish();
            //将buf中的数据写入protocolEncoderOutput中
            protocolEncoderOutput.write(output.getBuf());
        }

        @Override
        public void dispose(IoSession ioSession) throws Exception {
        }
    }

    private class WritableProtocolDecoder extends CumulativeProtocolDecoder{


        @Override
        protected boolean doDecode(IoSession ioSession, ByteBuffer byteBuffer, ProtocolDecoderOutput protocolDecoderOutput) throws Exception {
            //将byteBuffer中的数据解析进decodePack中
            DataPack decodePack = new DataPack();
            ByteBufferInput input = new ByteBufferInput(byteBuffer);
            //写入头部信息
            if (decodePack.getPayloadLength() < 0){
                if (byteBuffer.remaining() < HEADER_LEN){
                    return false;
                }else {
                    //写入请求数列号
                    decodePack.setSeq(input.readLong());
                    //设置传输数据大小
                    decodePack.setPayloadLength(input.readInt());
                }
            }
            //如果有传输的数据，查看byteBuffer中剩余的数据是否够头部信息中传的大小
            if (decodePack.getPayloadLength() >= 0){
                if (byteBuffer.remaining() < decodePack.getPayloadLength()){
                    return false;
                }
            }
            //获取list的大小
            int listSize = byteBuffer.getInt();
            //依次读取item的信息
            for (int i=0; i< listSize ; i++){
                //item对应的writable的类名
                String classname = input.readAcsiiString();
                IWritable writable = (IWritable) ClassUtils.newInstance(Class.forName(classname));
                //从input中读取item的信息
                writable.readFields(input);
                decodePack.add(writable);
            }
            //将decodePack写入到protocolDecoderOutput中
            protocolDecoderOutput.write(decodePack);
            return true;
        }

    }
}
