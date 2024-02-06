package com.example.e7tools.model;

public class E7Data {
    private byte[] data;
    private Long seq;

    public E7Data(byte[] data, Long seq) {
        this.data = data;
        this.seq = seq;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Long getSeq() {
        return seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }
}