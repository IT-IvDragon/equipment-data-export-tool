package com.example.e7tools.listener;

import com.example.e7tools.model.E7Data;
import org.pcap4j.core.PacketListener;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.util.*;

/**
 * todo:仅实现正常的功能，代码还需要优化
 */
public class CoreCaptureListener implements PacketListener {
    public final Map<Long, List<E7Data>> ackData = new HashMap<>();
    public final List<String> finalBuffer = new ArrayList<>();
    public static final Map<Integer, Object> loads = new HashMap<>();

    /* (non-Javadoc)
     * @see org.pcap4j.core.PacketListener#gotPacket(org.pcap4j.packet.Packet)
     */
    @Override
    public void gotPacket(Packet packet) {
        if (packet == null) {
            return;
        }
        //找到tcp包
        TcpPacket tcpPacket = packet.get(TcpPacket.class);
        if (tcpPacket == null) {
            return;
        }
        //找到负载
        Packet payload = tcpPacket.getPayload();
        if (payload == null) {
            return;
        }
        // 包含tcp头的数据
        byte[] rawData = tcpPacket.getRawData();

        // 提取实际包内容
        int headerLength = tcpPacket.getHeader().length();
        byte[] actualPayload = Arrays.copyOfRange(rawData, headerLength, rawData.length);

        //判断是否存在
        if (loads.containsKey(Arrays.hashCode(actualPayload))) {
            return;
        } else {
            loads.put(Arrays.hashCode(actualPayload), true);
        }

        long currentAck = tcpPacket.getHeader().getAcknowledgmentNumberAsLong();
        long currentSeq = tcpPacket.getHeader().getSequenceNumberAsLong();
        E7Data e7Data = new E7Data(actualPayload, currentSeq);
        if (ackData.containsKey(currentAck)) {
            ackData.get(currentAck).add(e7Data);
        } else {
            List<E7Data> list = new ArrayList<>();
            list.add(e7Data);
            ackData.put(currentAck, list);
        }
    }

    public void tryBuffer(long currAck) {
        List<E7Data> buffers = ackData.get(currAck);
        buffers.sort(Comparator.comparingLong(E7Data::getSeq));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (E7Data data : buffers) {
            byte[] buffer = data.getData();
            out.write(buffer, 0, buffer.length);
        }

        String hexStr = DatatypeConverter.printHexBinary(out.toByteArray()).toLowerCase();
        finalBuffer.add(hexStr);
    }

}
