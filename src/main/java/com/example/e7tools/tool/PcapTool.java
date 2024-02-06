package com.example.e7tools.tool;

import com.example.e7tools.listener.CoreCaptureListener;
import org.pcap4j.core.*;
import org.pcap4j.packet.Packet;

import java.io.File;
import java.util.List;

public class PcapTool {
    private boolean captureTag = true;

    public List<String> getFinalBuffer() {
        return finalBuffer;
    }

    private List<String> finalBuffer = null;

    public void setCaptureTag(boolean captureTag) {
        this.captureTag = captureTag;
    }

    /**
     * 根据IP获取指定网卡设备
     *
     * @return 指定的设备对象
     */
    public List<PcapNetworkInterface> getCaptureNetworkInterface() {
        List<PcapNetworkInterface> allDevs = null;
        try {
            // 获取全部的网卡设备列表，Windows如果获取不到网卡信息，输入：net start npf  启动网卡服务
            allDevs = Pcaps.findAllDevs();
        } catch (PcapNativeException e) {
            e.printStackTrace();
        }
        return allDevs;
    }

    /**
     * 开始抓包
     *
     * @param nif     选择的网卡
     * @param filter  过滤规则 "tcp and ( port 5222 or port 3333 )"
     * @param timeout 超时时间
     * @return
     */
    public void capture(PcapNetworkInterface nif, String filter, int timeout) {
        try {
            PcapHandle pcapHandle = nif.openLive(-1, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, timeout);
            pcapHandle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE);

            //循环抓包
            CoreCaptureListener coreCaptureListener = new CoreCaptureListener();
            while (captureTag) {
                Packet packet = pcapHandle.getNextPacket();
                coreCaptureListener.gotPacket(packet);
            }
            pcapHandle.close();
            coreCaptureListener.ackData.keySet().forEach(coreCaptureListener::tryBuffer);
            finalBuffer = coreCaptureListener.finalBuffer;
        } catch (PcapNativeException | NotOpenException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查有没有安装wpcap
     *
     * @return
     */
    public boolean checkWpcapDll() {
        String winPcapPath = System.getenv("WINDIR") + "\\System32\\drivers\\wpcap.dll";
        String npcapPath = System.getenv("WINDIR") + "\\System32\\Npcap\\wpcap.dll";
        return new File(winPcapPath).exists() || new File(npcapPath).exists();
    }
}
