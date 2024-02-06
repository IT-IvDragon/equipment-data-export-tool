package com.example.e7tools.constant;

/**
 * 抓包常量
 */
public class PcapConstant {
    public static final String DEFAULT_IP_TAG = "192.168";
    public static final String FILTER = "tcp and ( port 5222 or port 3333 )";

    public static final int TIMEOUT = 900000;
}
